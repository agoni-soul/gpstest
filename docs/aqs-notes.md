# AQS 源码笔记（倒数第三 / 倒数第二题）

记录时间：2026-09-02  
版本依据：OpenJDK 17 `java.util.concurrent.locks.AbstractQueuedSynchronizer`（Android 同套 Doug Lea
实现）  
说明：整理自连续问答中的两道 AQS
题，体例与 [thread-pool-executor-notes.md](thread-pool-executor-notes.md)
相同：以文字和源码为主，流程图只作为对应章节里的一小节。JDK 8 方法名是 `addWaiter` / `acquireQueued` /
`unparkSuccessor`；JDK 17 收进统一的 `acquire(...)`，算法相同。下文用面试更常见的 JDK 8 名字，并注明 17
的对应点。

对照：`ThreadPoolExecutor.Worker` 继承 AQS，见线程池笔记 §5。

---

## 目录

1. [AQS 底层调用机制](#1-aqs-底层调用机制)
2. [入队从哪端加、获取读哪个节点、释放怎么唤醒](#2-入队从哪端加获取读哪个节点释放怎么唤醒)

---

## 1. AQS 底层调用机制

对应问答：倒数第三题。

AQS 不实现「锁」本身，只提供一套模板：**你定义 `state` 何时算抢到 / 何时算释放，排队、阻塞、唤醒全由框架做。
** `Worker`、`ReentrantLock`、`CountDownLatch` 都是这套模板的不同填空。

### 1.1 三块零件

```text
┌─────────────────────────────────────────────┐
│  AbstractQueuedSynchronizer                 │
│                                             │
│  volatile int state          同步状态        │
│  Node head / tail            CLH 等待队列    │
│  exclusiveOwnerThread        独占持有者      │
└─────────────────────────────────────────────┘
```

**`state`（同步状态）** 是一个 `volatile int`，含义完全由子类决定：

| 实现               | `state` 含义                    |
|------------------|-------------------------------|
| `Worker`         | `-1` 未启动 / `0` 空闲 / `1` 正在跑任务 |
| `ReentrantLock`  | 重入次数，0 表示没人持锁                 |
| `Semaphore`      | 剩余许可数                         |
| `CountDownLatch` | 还剩几次 `countDown`              |

子类只能通过三个方法碰它（保证可见性 + CAS）：

```java
getState()

setState(newState)

compareAndSetState(expect, update)   // Unsafe CAS
```

**CLH 队列**：没竞争时 `head == tail == null`，懒创建。一有人要排队，先放一个 **dummy 哨兵** 当 `head`
。每个 `Node` 绑一条等待线程。JDK 8 用 `waitStatus`，JDK 17 用 `status`：

| 值                      | 含义                       |
|------------------------|--------------------------|
| `0`                    | 默认，还没要求被唤醒               |
| `SIGNAL` / `WAITING=1` | 后继已 park，释放时必须 unpark 后继 |
| `CANCELLED`（负数）        | 超时 / 中断，节点作废             |
| `CONDITION` / `COND`   | 在 Condition 队列，不在同步队列    |

独占节点、共享节点进 **同一条 FIFO 队列**。

**模板方法**（默认全抛 `UnsupportedOperationException`）。排队、park、unpark 全部是 `final`，子类改不了：

```java
tryAcquire(arg)          // 独占：能不能立刻拿到，true/false

tryRelease(arg)          // 独占：释放后要不要唤醒后继（完全释放才返回 true）

tryAcquireShared(arg)    // 共享：<0 失败，=0 成功但别再传播，>0 成功且可继续传播

tryReleaseShared(arg)

isHeldExclusively()      // Condition 用：是不是当前线程持锁
```

Doug Lea 把独占获取概括成：

```text
Acquire:
    while (!tryAcquire(arg)) {
        没在队列就入队;
        必要时 park 当前线程;
    }

Release:
    if (tryRelease(arg))
        unpark 队列里第一个等待者;
```

### 1.2 对外入口

独占（`ReentrantLock.lock` / `Worker.lock`）：

```java
public final void acquire(int arg) {          // 不响应中断
    if (!tryAcquire(arg))
        // JDK8: acquireQueued(addWaiter(EXCLUSIVE), arg)
        // JDK17: acquire(null, arg, shared=false, ...)
        入队并 park 直到 tryAcquire 成功;
}

public final boolean release(int arg) {
    if (tryRelease(arg)) {
        signalNext(head);   // JDK8: unparkSuccessor(head)
        return true;
    }
    return false;
}
```

还有三条变体，底层仍进同一个循环：

| 方法                     | 行为                                       |
|------------------------|------------------------------------------|
| `acquire`              | 一直等，中断只记下来，拿到锁后再 `selfInterrupt`         |
| `acquireInterruptibly` | park 期间被中断 → 出队并抛 `InterruptedException` |
| `tryAcquireNanos`      | 超时返回 false                               |

共享（`CountDownLatch.await` / `Semaphore.acquire`）是 `acquireShared` / `releaseShared`，循环里调的是
`tryAcquireShared`，成功后还会 **继续唤醒后面的共享节点**（传播）。

`arg` 对 AQS 没意义，原样传给 `tryAcquire`。`ReentrantLock` / `Worker` 传 `1`；`Semaphore` 传要扣的许可数。

### 1.3 快路径：Worker 日常就走这条

`Worker.lock()` → `acquire(1)`：

```java
public void lock() {
    acquire(1);
}

public final void acquire(int arg) {
    if (!tryAcquire(arg))    // 成功就直接返回，不建队列
        ...入队 park...
}
```

`Worker.tryAcquire`：

```java
protected boolean tryAcquire(int unused) {
    if (compareAndSetState(0, 1)) {          // 只有 0→1
        setExclusiveOwnerThread(Thread.currentThread());
        return true;
    }
    return false;                            // 已被占用，不会重入
}
```

`runWorker` 里先 `unlock()` 把构造时的 `-1` 打成 `0`，再每次跑任务 `lock()`：

```text
构造:     setState(-1)     interruptIfStarted 要求 state>=0，start 前的 interrupt 被吞
runWorker: unlock()        tryRelease → state=0，允许 interrupt
跑任务前:  lock()          CAS 0→1，标记「忙」
跑任务后:  unlock()        state=0，标记「闲」
```

`shutdown()` 的 `interruptIdleWorkers` 用的是 `tryLock()` → `tryAcquire`，**失败就立刻返回，不会入 AQS
队列**。所以 Worker 这把锁几乎不会出现排队；AQS 在这里主要当 **不可重入状态机 + tryLock 探测忙闲**。

对比 `ReentrantLock`：`tryAcquire` 发现 `owner == 当前线程` 就把 `state++`，这才是可重入。Worker
故意不写这步。

### 1.4 慢路径：入队 + park

`tryAcquire` 失败之后：

```text
acquire(arg)
  ├─ tryAcquire(arg) 失败
  ├─ addWaiter(Node.EXCLUSIVE)     把当前线程包成 Node 接到 tail
  └─ acquireQueued(node, arg)      自旋/park，直到成为头后继并抢到锁
        若 park 期间被中断 → 返回 true，acquire 末尾 selfInterrupt()
```

**`addWaiter`：CAS 接到队尾**

```java
private Node addWaiter(Node mode) {
    Node node = new Node(Thread.currentThread(), mode);
    Node pred = tail;
    if (pred != null) {
        node.prev = pred;
        if (compareAndSetTail(pred, node)) {   // 只 CAS tail
            pred.next = node;                  // next 不是原子的
            return node;
        }
    }
    enq(node);   // tail==null 或 CAS 失败：自旋初始化 / 重试
    return node;
}

private Node enq(Node node) {
    for (; ; ) {
        Node t = tail;
        if (t == null) {
            if (compareAndSetHead(new Node()))  // dummy 哨兵
                tail = head;
        } else {
            node.prev = t;
            if (compareAndSetTail(t, node)) {
                t.next = node;
                return t;
            }
        }
    }
}
```

要点：入队的同步点只有 CAS tail；`next` 稍后由入队线程写；dummy `head` 不代表任何线程。

**`acquireQueued`：只有队头后继才有资格再试**

```java
final boolean acquireQueued(final Node node, int arg) {
    boolean interrupted = false;
    for (; ; ) {
        final Node p = node.predecessor();
        if (p == head && tryAcquire(arg)) {   // 前驱是 head = 轮到我了
            setHead(node);                    // 自己变成新 head（哨兵换人）
            p.next = null;
            return interrupted;
        }
        if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
            interrupted = true;
    }
}
```

不是 FIFO 严格禁插队：刚进来的线程在 `acquire()` **入队前**已经 `tryAcquire` 过一次（可以插队成功，即
barge）。公平锁在 `tryAcquire` 里加 `hasQueuedPredecessors()`，看到前面有人就返回 false。

**`shouldParkAfterFailedAcquire`：告诉前驱「我会 park，请 unpark 我」**

```java
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus;
    if (ws == Node.SIGNAL)
        return true;                         // 前驱已经答应会唤醒，可以 park
    if (ws > 0) {
        do {                                 // 前驱 CANCELLED，沿 prev 跳过废节点
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        // 不立刻 park，再回到循环 tryAcquire 一次
    }
    return false;
}
```

先把前驱标成 `SIGNAL`，再重新 `tryAcquire`，失败才 `LockSupport.park(this)`。避免：释放线程看见
`waitStatus==0` 觉得没人要唤醒就走了，等待线程然后才 park → 永久睡死。

**真正阻塞**

```java
private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this);
    return Thread.interrupted();
}
```

`park` / `unpark` 是许可模型，不是「必须先 park 再 unpark」：`unpark` 可以先发生，下次 `park`
立刻返回。这也是为什么要 `SIGNAL` + 再试一次。底层是 `Unsafe.park`。

### 1.5 释放、共享、取消、Condition

```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```

`Worker.tryRelease` 永远把锁打到 0 并返回 true（不可重入，一次就完全释放）。`ReentrantLock` 则是
`state -= 1`，减到 0 才返回 true；重入未减完不唤醒后继。

`unparkSuccessor`（JDK 17 叫 `signalNext`）只唤醒 **一个** 后继。被唤醒的线程回到 `acquireQueued` 循环，再次
`tryAcquire`：成功则 `setHead(自己)`；失败（被新来的线程插队抢走）再 park。独占模式 **不会** 连锁唤醒整条队列。

共享模式差在 `tryAcquireShared` 的三态返回值：

```text
< 0  没抢到，去排队
= 0  抢到了，但资源刚好用尽，不必唤醒后面的共享者
> 0  抢到了，后面可能还能抢，必须传播唤醒
```

`CountDownLatch`：`state` 是计数。`await` → `tryAcquireShared` 看 `state==0`；`countDown` →
`tryReleaseShared` 把计数减到 0 再唤醒。JDK 17 拿到锁后若 `shared`，会 `signalNextIfShared`，后继也是
`SharedNode` 才继续 unpark。

取消：超时或 `acquireInterruptibly` 被中断 → `cancelAcquire` 把节点打成 `CANCELLED`，从 tail
往前摘掉废节点；若废节点曾是队头后继，要 unpark 新的后继，否则队列会断。

Condition（`await` / `signal`）是 **另一条单向链表**，不跟同步队列混：

```text
await():
  1. 把当前线程接到 Condition 队列
  2. release(savedState)     完全释放锁，唤醒同步队列后继
  3. park 直到 signal
  4. signal 把节点从 Condition 队列搬到同步队列
  5. acquire(savedState)     重新抢锁（重入次数一并恢复）
```

所以 `await` 不是「睡在锁上」，而是先释放锁，再睡在条件队列，被 signal 后再去同步队列排队抢锁。

### 1.6 和 Worker 对上

```text
池线程 runWorker:
  w.unlock()
      AQS.release(1)
        Worker.tryRelease → state=0, owner=null
        signalNext(head)     通常队列空，什么也不做

  跑任务前 w.lock()
      AQS.acquire(1)
        Worker.tryAcquire CAS 0→1 成功     ← 99% 走这里就返回
        失败才会 ExclusiveNode 入队 + park

  shutdown 线程 interruptIdleWorkers:
      w.tryLock()            // 只 tryAcquire，失败不排队
        成功（state 仍是 0，Worker 在 getTask 里闲着）
          t.interrupt()      叫醒 take()/poll()
          w.unlock()
        失败（Worker 正在 task.run，state==1）
          不 interrupt，任务跑完

  跑任务后 w.unlock()  同第一次，state 回到 0
```

`interruptIfStarted` 看的也是这颗 `state`：`getState() >= 0` 才 interrupt。这颗 `state` 是 **AQS 的同步状态
**，不是 CLH `Node.status`。

三句话：

1. AQS 管排队和睡觉，`state` 的语义全在子类的 `try*`。
2. 独占：释放只 unpark 一个后继；共享：成功后还可能往下传播。
3. `Worker` 把 `state` 当忙闲标志，几乎走快路径；真正把 AQS 队列跑满的是 `ReentrantLock` / `Semaphore`
   这种高竞争锁。

### 1.7 调用链流程图

下面这张图只收口 §1 的调用顺序，细节仍以上面源码为准。

```text
业务:  lock() / await() / acquireShared()
          │
          ▼
AQS 模板: acquire / release / acquireShared / releaseShared   （final）
          │
          ├─ 快路径: tryAcquire*(arg) 成功 → 改 state，结束
          │
          └─ 慢路径:
                建 Node，CAS 接到 tail
                前驱标 SIGNAL
                再 tryAcquire 一次（防丢唤醒）
                LockSupport.park
                    ▲
                    │ unpark
                释放方: tryRelease* 成功
                        unparkSuccessor(head)
                          ├─ 正常：unpark(head.next)
                          └─ next 空/已取消：从 tail 沿 prev 往前找
                被唤醒者再 tryAcquire*，成功则自己变成 head

Worker 对照（几乎总走快路径）:

  unlock() → release(1) → tryRelease → state=0
  lock()   → acquire(1) → tryAcquire CAS 0→1 → 返回
  shutdown tryLock() 失败则不入队、不 interrupt 正在跑的任务
```

---

## 2. 入队从哪端加、获取读哪个节点、释放怎么唤醒

对应问答：倒数第二题。

三件事对应队列上三个方向，别混在一块：

| 动作           | 动的是哪一端                  | 会不会扫整条链                       |
|--------------|-------------------------|-------------------------------|
| **入队（添加）**   | 只 CAS **tail**，接到队尾     | 不会从头读                         |
| **获取（读/抢锁）** | 只看自己的 **prev 是不是 head** | 正常路径 O(1)，不遍历                 |
| **释放（唤醒）**   | 唤醒 **head 的后继**（第一个等待者） | `next` 断了或已取消，才从 **tail 往前**找 |

「从后往前」只出现在 **修补 / 找不到后继** 时，不是入队、也不是正常获取。

### 2.1 添加：永远接在 tail 后面

JDK 8 `addWaiter`：

```java
private Node addWaiter(Node mode) {
    Node node = new Node(Thread.currentThread(), mode);
    Node pred = tail;                          // 只读尾，不读 head
    if (pred != null) {
        node.prev = pred;
        if (compareAndSetTail(pred, node)) {   // 同步点：CAS tail
            pred.next = node;
            return node;
        }
    }
    enq(node);                                 // tail==null 或 CAS 失败
    return node;
}
```

JDK 17 写在统一 `acquire` 循环里，意思一样：读 `tail`，`casTail`，失败就重试；`tail==null` 时先
`tryInitializeHead()` 造 dummy。

```text
无竞争:  head == tail == null，不建队列

第一次有人要排队:
  先 new dummy 哨兵，CAS 成 head，再 tail=head
  然后把自己 CAS 成新 tail

  head(哨兵) ←── prev ── 你(node) = tail
       └── next ──►
```

多人同时入队：大家都抢 `casTail(旧tail, 自己)`。失败者把 `prev` 清掉再读新 `tail` 重试。  
**不会**从 head 往后扫，也 **不会**插到队头。队头后继（第一个等待者）是最早 CAS 成功的那个人。

`next` 不是 CAS 的，由入队线程事后写 `pred.next = node`。所以释放时可能暂时看到 `head.next == null`
，这时才需要从 tail 往回找——这是释放的事，入队不管。

### 2.2 获取：不读「第一个」，只问「我的前驱是不是 head」

入队后进 `acquireQueued`：

```java
final boolean acquireQueued(final Node node, int arg) {
    for (; ; ) {
        final Node p = node.predecessor();     // 只读 node.prev
        if (p == head && tryAcquire(arg)) {    // 前驱是哨兵 = 我是第一个等待者
            setHead(node);                     // 自己变成新 head
            p.next = null;
            return interrupted;
        }
        if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
            interrupted = true;
    }
}
```

```java
Node predecessor() {
    Node p = prev;
    if (p == null) throw new NullPointerException();
    return p;
}
```

含义：

```text
head(哨兵) → A → B → C(tail)

A.prev == head  → A 可以 tryAcquire，别人不行
B 只看 B.prev==A，不是 head，直接准备 park
C 同理

不从头遍历「谁是第一个」，
也不从尾遍历。每个节点只认自己的 prev。
```

抢到之后 `setHead(node)`：

```java
private void setHead(Node node) {
    head = node;
    node.thread = null;   // 当新哨兵，不再代表等待线程
    node.prev = null;
}
```

A 成功后队列变成：`head(A当哨兵) → B → C`。B 被 unpark 醒来，发现 `B.prev == head`，才轮到 B
`tryAcquire`。

所以「读取」是 **各自看前驱**，不是「去读第一个节点」。谁是第一个，等价于 **谁的 prev 恰好是当前 head**。

`shouldParkAfterFailedAcquire` 里有一段会 **顺着 prev 往前跳**：那是前驱 `CANCELLED`
时把废节点摘掉，仍然不是「从后往前读整队」。

公平锁的 `hasQueuedPredecessors()` 才是「直接看第一个等待者」：看 `head.next` 上的 waiter
是不是自己。这是公平策略用的探针，不是每个 `acquire` 的主路径。

### 2.3 释放：先读 head.next；断了才从 tail 往前

```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```

```java
private void unparkSuccessor(Node node) {      // node = 当前 head
    int ws = node.waitStatus;
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);  // 清掉 SIGNAL

    Node s = node.next;                        // ★ 先直接读第一个后继
    if (s == null || s.waitStatus > 0) {       // next 还没写上，或后继已取消
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;                         // ★ 从尾巴往前，留下最靠前的有效节点
    }
    if (s != null)
        LockSupport.unpark(s.thread);
}
```

**正常：直接读第一个**

```text
head(哨兵) → A(waitStatus≤0) → B → C
             ▲
        head.next == A，unpark(A)
```

释放线程 **不遍历**。A 醒来后走上面 §2.2：`A.prev == head`，`tryAcquire` 成功再 `setHead(A)`。

**异常：从后往前扫**

`head.next == null` 的典型竞态：

```text
线程1 刚 CAS 成新 tail，prev 已指向 head，但还没执行 pred.next = node
释放线程 此时读 head.next → null
```

或 `head.next` 指向的节点已经 `CANCELLED`。这时不能信 `next`，改用 `prev` 链（入队时 `prev` 是先写、且跟着
CAS tail 的，更可靠）：

```text
从 tail 往 prev 走：C → B → (作废) → A
每次碰到 waitStatus≤0 就记下
走出循环时 s 是「最靠近 head 的那个有效等待者」→ unpark 它
```

这就是「从后往前读」的 **唯一主场景**。JDK 17 的 `cleanQueue` 取消节点时同样从 `tail` 往 `prev` 清。

共享释放 `releaseShared` 先同样叫醒 `head` 的后继；该节点 `tryAcquireShared` 成功后若还是
Shared，再往后传，仍然是 **顺着 next 一个一个**，不是从尾巴扫。

### 2.4 对照原问法

- **添加不是从后往前读，是直接 CAS 接到最后一个（tail）。**
- **获取也不是去读第一个元素，是每个等待者看「我的 prev 是不是 head」；是才有资格 `tryAcquire`。**
- **释放才「读第一个」：唤醒 `head.next`；只有这条指针不可靠时，才从 tail 沿 `prev` 往前找。**

### 2.5 三端方向流程图

下面这张图只收口 §2 的三个方向，细节仍以上面源码为准。

```text
队列:  head(哨兵) → A → B → C(tail)


添加 addWaiter / casTail
  只碰 tail，不读 head
  新来的 D：CAS tail C→D
  失败则重读新 tail 再 CAS
  D 成为新 tail，不会插到队头


获取 acquireQueued
  每个节点只读自己的 prev
  A.prev==head ?  是 → tryAcquire → 成功则 A 变新 head
  B、C 不是队头 → 标前驱 SIGNAL → park
  不遍历「谁是第一个」


释放 unparkSuccessor(head)
  先读 head.next  → 正常就是 A，unpark(A)
  next 空/已取消  → for (t=tail; t!=head; t=t.prev) 找最前有效节点
  A 醒来后回到「获取」那条路
```
