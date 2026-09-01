# ThreadPoolExecutor 源码笔记（问答 1 / 2 / 3 / 5 / 6）

记录时间：2026-09-02  
版本依据：OpenJDK 17 `java.util.concurrent.ThreadPoolExecutor`（Android 同套 Doug Lea
实现，方法名一致）  
说明：整理自连续问答中的第 1、2、3、5、6 题（跳过第 4 题「全项目几个池 / 第三方能否复用」）。  
示例代码：

- `app/src/main/java/com/haha/main/thread/ThreadTest.kt`
- `ServiceRouterUtils/src/main/java/com/haha/servicerouterutils/utils/DefaultPoolExecutor.java`

---

## 目录

1. [七个形参、每个参数含义，以及 `execute` 底层](#1-七个形参每个参数含义以及-execute-底层)
2. [
   `core=10, max=20, ArrayBlockingQueue(5), CallerRunsPolicy`：第 5 / 15 / 23 / 26 个任务](#2-core10-max20-arrayblockingqueue5-callerrunspolicy第-5--15--23--26-个任务)
3. [执行顺序怎么理解才对](#3-执行顺序怎么理解才对)
4. [`ctl` 装的是什么、
   `ctl.get()` 拿到哪一段、刚 init 是什么状态](#4-ctl-装的是什么ctlget-拿到哪一段刚-init-是什么状态)
5. [内部类 `Worker`：核心线程、临时线程、
   `workers` 里的线程怎么被拉起来跑](#5-内部类-worker核心线程临时线程workers-里的线程怎么被拉起来跑)

---

## 1. 七个形参、每个参数含义，以及 `execute` 底层

对外有 4 个重载构造，另外 3 个只是给 `threadFactory` / `handler` 填默认值，最终都进 **7 形参** 构造。

```java
public ThreadPoolExecutor(
        int corePoolSize,
        int maximumPoolSize,
        long keepAliveTime,
        TimeUnit unit,
        BlockingQueue<Runnable> workQueue,
        ThreadFactory threadFactory,
        RejectedExecutionHandler handler)
```

校验：`corePoolSize < 0`、`maximumPoolSize <= 0`、`maximumPoolSize < corePoolSize`、
`keepAliveTime < 0` → `IllegalArgumentException`；队列 / factory / handler 为 null → NPE。  
`keepAliveTime` 立刻转成纳秒：`this.keepAliveTime = unit.toNanos(keepAliveTime)`。

### 1.1 每个参数

| # | 形参                | 含义                                                                            |
|---|-------------------|-------------------------------------------------------------------------------|
| 1 | `corePoolSize`    | 核心人数。`workerCount < core` 时 **宁可新开线程，也不入队**（哪怕已有空闲核心）                         |
| 2 | `maximumPoolSize` | 池内线程上限。只有 `offer` 失败之后才扩到这个值                                                  |
| 3 | `keepAliveTime`   | **超额**线程空闲多久回收（默认不管核心；`allowCoreThreadTimeOut(true)` 后核心也适用）                  |
| 4 | `unit`            | 上面那个时间的单位                                                                     |
| 5 | `workQueue`       | 核心满了之后任务先躺的地方；队列形态决定 max 会不会生效                                                |
| 6 | `threadFactory`   | `new Thread`。`Worker` 自己是 `Runnable`，工厂造的 `Thread` 的 target 是 Worker，不是你提交的任务 |
| 7 | `handler`         | 队列满且人数已到 max（或已 shutdown）时怎么处理这个 `Runnable`                                   |

项目里两处对照：

```29:45:app/src/main/java/com/haha/main/thread/ThreadTest.kt
        ThreadPoolExecutor(
            3,
            5,
            60L,
            TimeUnit.SECONDS,
            ArrayBlockingQueue(4),
            { r ->
                val t = Thread(r, "ThreadTest-${taskSeq.incrementAndGet()}")
                ...
            },
            { r, e ->
                ThreadPoolExecutor.CallerRunsPolicy().rejectedExecution(r, e)
            },
        )
```

`DefaultPoolExecutor`：`core == max == CPU+1`，`ArrayBlockingQueue(64)`，拒绝只打 log。此时 **max 形同虚设
**（有界队列填满前永远 `offer` 成功，走不到扩临时线程）。

队列三种性格：

| 队列                       | `offer`         | 临时线程                                              |
|--------------------------|-----------------|---------------------------------------------------|
| `SynchronousQueue`       | 没人正在 `take` 就失败 | 核心一满立刻扩到 max（OkHttp Dispatcher：`core=0, max=MAX`） |
| 无界 `LinkedBlockingQueue` | 永远成功            | 扩不到 max                                           |
| `ArrayBlockingQueue(n)`  | 有空位才成功          | 队列满了才扩                                            |

拒绝策略：`AbortPolicy`（默认抛异常）、`CallerRunsPolicy`（调用线程自己 `run`）、`DiscardPolicy`（丢）、
`DiscardOldestPolicy`（丢队头再 `execute`）。

### 1.2 `execute` 三步（提交侧）

```java
public void execute(Runnable command) {
    if (command == null) throw new NullPointerException();
    int c = ctl.get();
    if (workerCountOf(c) < corePoolSize) {
        if (addWorker(command, true))          // ① 开核心，任务当 firstTask
            return;
        c = ctl.get();
    }
    if (isRunning(c) && workQueue.offer(command)) {   // ② 入队
        int recheck = ctl.get();
        if (!isRunning(recheck) && remove(command))
            reject(command);                   // 入队后被 shutdown，回滚
        else if (workerCountOf(recheck) == 0)
            addWorker(null, false);            // 任务进队但一个 worker 都没有，补空线程去捞
    } else if (!addWorker(command, false))     // ③ offer 失败，上限换成 max
        reject(command);
}
```

口诀：**先填 core → 再填队列 → 再填 max → 最后 handler。** 不是「先排队再用空闲核心」。

`addWorker(firstTask, core)`：`core==true` 用 `corePoolSize` 当上限，`false` 用 `maximumPoolSize`。先
CAS `workerCount++`，再 `new Worker(firstTask)`、`workers.add`、`t.start()`。

消费侧：`Worker.run()` → `runWorker` → 先跑 `firstTask`，再 `getTask()` 从队列拿。核心默认
`workQueue.take()` 永久阻塞；`workerCount > core` 时 `poll(keepAliveTime)`，超时退出。

---

## 2. `core=10, max=20, ArrayBlockingQueue(5), CallerRunsPolicy`：第 5 / 15 / 23 / 26 个任务

口径：按第 N 个 **提交的任务** 讲。`max=20` 造不出第 23 / 26 条池线程。

前提：单线程依次 `execute`，任务都还没跑完；池一直 `RUNNING`；未开 `allowCoreThreadTimeOut`。  
`execute` 用的是 `offer()`（非阻塞），队列满返回 `false`，**不会**在队列上堵住提交线程。

容量：`20 个在跑 + 5 个在排队 = 25`，第 26 个起 CallerRuns。

| 第几个任务 | 提交前          | 走哪一步                       | 提交后            |
|-------|--------------|----------------------------|----------------|
| 1～10  | wc=0～9，队列空   | ① `addWorker(task, true)`  | 10 条核心         |
| 11～15 | wc=10，队列 0～4 | ② `offer` 成功               | 队列 1～5（满）      |
| 16～25 | wc=10～19，队列满 | ③ `addWorker(task, false)` | wc=11～20       |
| 26 起  | wc=20，队列满    | `reject` → CallerRuns      | 调用线程自己 `run()` |

### 2.1 第 5 个：开第 5 条核心线程

提交前 `wc=4`。`4 < 10` → `addWorker(command, true)`：CAS `wc` 4→5，`new Worker(第5个任务)`，
`t.start()`，`runWorker` 第一轮直接 `firstTask.run()`，**不入队**。

### 2.2 第 15 个：把队列填满，不开新线程

提交前 `wc=10`，队列已有任务 11～14（size=4）。`10 < 10` 为假，跳过 ①。`ArrayBlockingQueue.offer` 成功，队列变成
5 **满**。二次检查：仍 RUNNING、`wc≠0`，什么都不做。  
`maximumPoolSize=20` 此时没用——核心满了优先入队，队列没满绝不扩容。该任务等某条核心跑完后
`getTask()` → `take()` 捞走。

### 2.3 第 23 个：队列已满，开第 18 条线程（临时）

提交前：任务 15 后 `wc=10, q=5`；16～22 各开一条临时 → **`wc=17`**。  
`offer` 失败 → `addWorker(command, false)`：`17 >= 20` 为假，CAS 17→18，`firstTask` 立刻跑。  
新线程是池里第 **18** 条（核心 10 + 临时 8），不是第 23 条。任务 16～25 都走这条路，直到 `wc=20`。

### 2.4 第 26 个：饱和，调用线程自己跑

提交前 `wc=20`、队列满（任务 25 占掉最后一条临时线程）。`addWorker(..., false)` 里 `20 >= 20` 立刻
false，不 `new Worker`。

```java
final void reject(Runnable command) {
    handler.rejectedExecution(command, this);
}

// CallerRunsPolicy
public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
    if (!e.isShutdown()) {
        r.run();     // 当前调用线程同步执行，不是池线程
    }
}
```

不占 `workerCount`、不进队列；`execute()` 阻塞到第 26 个任务结束（反压）。已 shutdown 则直接丢弃。

---

## 3. 执行顺序怎么理解才对

大方向对：**先核心 → 再队列 → 再临时线程到 max → 最后拒绝策略。** 有四处必须改口。

**1. 不是「有核心线程就拿来跑」，而是「核心人数不够就再开一条」。**  
`workerCount < core` 时，哪怕已有核心空闲，也 `new` 一条，任务当 `firstTask`，不入队。  
「复用空闲核心」只发生在核心已经满了之后：空闲核心在 `getTask()` 里 `take()` / `poll()` 拿队列任务。

**2. 不是「有没有队列」，构造时就必须传队列。**  
差的是 `offer` 成不成功（有空位 / `SynchronousQueue` 有没有消费者 / 无界则永远成功）。

**3. 队列里躺的是任务（Runnable），不是线程。**  
不会「把队列线程加入到核心线程」。核心跑完自己的活，自己去队列 **拿任务过来跑**，还是原来那条线程。

**4. 最后饱和的是「任务提交」，不是「再加入线程」。**  
`CallerRunsPolicy` 是调用线程自己 `run()`，并不再开池线程。

更准确的比喻：

```text
人不够 core，就招核心员工干活（不看在不在空闲）；
核心招满了，活先放桌上（队列）；
桌子也满了，再招临时工，直到 max；
工位和桌子都满了，老板（RejectedExecutionHandler）决定：扔掉 / 调用者自己干 / 抛异常。
桌上的活由已经在岗的人空下来自己拿。
```

---

## 4. `ctl` 装的是什么、`ctl.get()` 拿到哪一段、刚 init 是什么状态

`ctl.get()` **不是在 5 个状态里挑一个返回**。它拿到的是 **一整颗打包过的 `int`**：高 3 位运行状态，低
29 位当前 worker 数。

```java
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

private static final int COUNT_BITS = 29;                 // 32 - 3
private static final int COUNT_MASK = (1 << 29) - 1;

private static final int RUNNING = -1 << COUNT_BITS;   // 高 3 位 111，负数
private static final int SHUTDOWN = 0 << COUNT_BITS;
private static final int STOP = 1 << COUNT_BITS;
private static final int TIDYING = 2 << COUNT_BITS;
private static final int TERMINATED = 3 << COUNT_BITS;

runStateOf(c)    =c &~COUNT_MASK;   // 取出状态

workerCountOf(c) =c &COUNT_MASK;   // 取出人数

ctlOf(rs, wc)    =rs |wc;           // 拼回去
```

```text
  31 30 29  28 ............................. 0
 ┌───┬───┬───┬──────────────────────────────┐
 │     runState    │      workerCount       │
 └─────────────────┴────────────────────────┘
```

`execute()` 每次都是先 `get` 再拆：

```java
int c = ctl.get();                       // 整包
if(

workerCountOf(c) <corePoolSize)... // 用人数
        if(

isRunning(c) &&workQueue.

offer(...))...  // 用状态；RUNNING 是唯一负数，c < SHUTDOWN 即还在跑
```

CAS 改状态或改人数必须两半一起写：`ctl.compareAndSet(c, ctlOf(SHUTDOWN, workerCountOf(c)))`。

状态机：`RUNNING → SHUTDOWN → STOP → TIDYING → TERMINATED`。  
`shutdown()` 不接新任务、队列继续跑；`shutdownNow()` 连队列也不跑、打断正在执行的。

**刚 init：** 字段初始化就是 `ctlOf(RUNNING, 0)`，构造函数不再改 `ctl`。

```text
ctl.get()                = RUNNING | 0 = -1 << 29 = 0xE0000000
runStateOf(ctl.get())    = RUNNING
workerCountOf(ctl.get()) = 0
isRunning(ctl.get())     = true
```

默认不预创建线程。第一条 `execute` 看到 `0 < core`，`addWorker` 把低 29 位 0→1，高 3 位仍是 RUNNING。

---

## 5. 内部类 `Worker`：核心线程、临时线程、`workers` 里的线程怎么被拉起来跑

**核心线程和临时线程在源码里是同一种 `Worker`，没有两个子类。** 差别只在 `addWorker(..., core)`
用哪个人数上限，以及空闲时 `getTask()` 要不要超时退出。  
真正被调度执行的是 `Worker.thread`；`workers` 只是花名册。

### 5.1 Worker 三个角色叠一个对象

```java
private final class Worker extends AbstractQueuedSynchronizer implements Runnable {
    final Thread thread;
    Runnable firstTask;
    volatile long completedTasks;

    Worker(Runnable firstTask) {
        setState(-1);                                      // 未进 runWorker 前禁止 interrupt
        this.firstTask = firstTask;
        this.thread = getThreadFactory().newThread(this);  // Thread 的 target 是这个 Worker
    }

    public void run() {
        runWorker(this);
    }
}
```

你提交的 `Runnable` 不是这条线程的 target。任务是 `firstTask` 或后来 `getTask()` 拿到的，在
`runWorker` 里 `task.run()`。

AQS `state` 当忙闲锁（不可重入，故意不用 `ReentrantLock`）：

| state | 含义                              | shutdown 能否 interrupt                                    |
|-------|---------------------------------|----------------------------------------------------------|
| `-1`  | 刚构造                             | `interruptIfStarted` 要求 `state>=0`，start 前的 interrupt 被吞 |
| `0`   | 空闲（在 `getTask` 里 `take`/`poll`） | `tryLock` 成功，可以 interrupt 叫醒                             |
| `1`   | 正在 `task.run()`                 | `tryLock` 失败，`shutdown()` 不打断正在跑的任务                      |

`shutdownNow()` 走 `interruptIfStarted()`，连干活的也打断。

### 5.2 唯一创建路径：`addWorker`

```text
execute / prestart / processWorkerExit 补人
        │
        ▼
addWorker(firstTask, core)
  ① CAS：ctl 里 workerCount++
  ② new Worker(firstTask)
  ③ mainLock 下 workers.add(w)     // HashSet<Worker>
  ④ t.start()                      // 池线程真正启动的唯一时刻
```

`core` **不会写进 Worker 对象**：

| 调用                          | firstTask | core  | 谁调                       |
|-----------------------------|-----------|-------|--------------------------|
| `addWorker(command, true)`  | 你的任务      | true  | `execute` ①：wc < core    |
| `addWorker(command, false)` | 你的任务      | false | `execute` ③：队列满，扩到 max   |
| `addWorker(null, false)`    | 无         | false | 入队后 wc==0；或 worker 退出后补人 |

`workers` 必须拿 `mainLock` 才能改。用来 shutdown 遍历 interrupt、`getPoolSize()`、记
`largestPoolSize`。  
**执行任务不靠遍历 `workers`。** 线程 `start()` 后自己在 `runWorker` 循环干活；队列任务由已启动线程自己
`take()`。

### 5.3 `runWorker`：一条线程跑很多个任务

```java
final void runWorker(Worker w) {
    Runnable task = w.firstTask;
    w.firstTask = null;
    w.unlock();                          // state -1→0，允许 interrupt
    boolean completedAbruptly = true;
    try {
        while (task != null || (task = getTask()) != null) {
            w.lock();                    // 标记忙
            beforeExecute(wt, task);
            try {
                task.run();              // ★ 业务
                afterExecute(task, null);
            } catch (Throwable ex) {
                afterExecute(task, ex);
                throw ex;
            } finally {
                task = null;
                w.completedTasks++;
                w.unlock();
            }
        }
        completedAbruptly = false;
    } finally {
        processWorkerExit(w, completedAbruptly);
    }
}
```

第一轮跑 `firstTask`；之后靠 `getTask()` 阻塞拿下一个。线程不退出，只换 `task`。

### 5.4 核心 vs 临时：对象相同，空闲策略不同

```java
boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
Runnable r = timed
        ? workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS)
        : workQueue.take();
```

`wc ≤ core` → `take()` 一直等（口头：核心）；`wc > core` → `poll` 超时退出（口头：临时）。  
超时退出的不一定是最后创建的那条，谁先 `poll` 超时谁走。`getTask()` 返回 `null` 后 `runWorker` 跳出 →
`processWorkerExit`：从 `workers` 删除；异常死亡或人数不够 core / 队列非空没人了，再
`addWorker(null, false)` 补一条。

### 5.5 三种「被调用执行」

仍用 `core=10, max=20, ArrayBlockingQueue(5)`：

```text
第 5 个任务（核心）
  addWorker(task, true) → workers.add → t.start()
  runWorker 第一轮 firstTask.run()，不入队
  之后 take() 常驻等队列

第 11～15 个（队列任务）
  只 offer，不 start 新线程
  已有 Worker 跑完后 getTask().take() 捞走，仍是原来那条核心线程

第 23 个（临时）
  offer 失败，addWorker(task, false)
  同样 new Worker + start + firstTask.run()
  跑完后 wc>core → poll(keepAlive)，超时从 workers 移除
```

`execute` 入队后若发现 `workerCount==0`：`addWorker(null, false)`，新线程 `firstTask==null`，立刻
`getTask()` 把刚入队的任务拿来跑。这是唯一「先入队、再补线程」的路径。

忙闲：`w.isLocked()==true` 正在 `task.run()`；`false` 在 `getTask()` 等待。`getActiveCount()` 就是数
`isLocked()` 的个数。

### 5.6 生命周期总图

```text
execute(task)
    ├─ wc < core ──────────────► addWorker(task, true)   ─┐
    ├─ offer 成功 ─────────────► 任务进队列                │  已有 Worker.take() 捞走
    │     若 wc==0 再补 addWorker(null, false)            │
    └─ offer 失败且 wc < max ──► addWorker(task, false)  ─┤
                              否则 reject                  │
                                                           ▼
                                              new Worker(firstTask?)
                                              workers.add
                                              thread.start()
                                                           │
                                                           ▼
                                              Worker.run() → runWorker
                                                ├─ firstTask.run()
                                                └─ loop: getTask()
                                                     take()   wc≤core
                                                     poll()   wc>core，超时退出
                                                     task.run()
                                                getTask()==null
                                                           │
                                                           ▼
                                              processWorkerExit
                                              workers.remove
                                              必要时再 addWorker(null, false)
```

三句话：

1. `Worker` = 一条线程 + 一把忙闲锁 + 可选的 firstTask，核心/临时是同一类对象。
2. 任务被执行只有两种：创建时当 `firstTask` 立刻 `run`，或已被 `start` 的 Worker 从队列 `getTask` 再
   `run`。
3. `workers` 是活着的 Worker 集合，不负责派发；派发在 `execute` 的「开线程 / 入队」和 Worker 自己的
   `getTask` 循环里完成。
