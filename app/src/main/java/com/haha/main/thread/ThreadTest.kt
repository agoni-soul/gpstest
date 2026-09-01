package com.haha.main.thread

import android.util.Log
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * @auther: haha
 * @Date:   2025/8/16
 * @Detail: 线程池测试。对照 OkHttp Dispatcher：
 *          execute() 当前线程同步跑；enqueue() 丢进线程池异步跑。
 */
class ThreadTest {
    private val TAG = this.javaClass.simpleName
    private val taskSeq = AtomicInteger(0)

    /**
     * 对齐 OkHttp Dispatcher 的队列形态（SynchronousQueue：不排队，来一个立刻找线程）。
     * 差别：OkHttp 是 core=0 / max=MAX，并发靠 maxRequests 限；这里用 3/5 方便观察扩容和拒绝。
     *
     * core=3：空闲核心线程常驻
     * max=5：核心忙完再临时扩到 5
     * keepAlive=60s：非核心空闲回收（对照 OkHttp Dispatcher）
     */
    val mExecutor: ThreadPoolExecutor by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ThreadPoolExecutor(
            3,
            5,
            60L,
            TimeUnit.SECONDS,
            ArrayBlockingQueue(4),
            { r ->
                val t = Thread(r, "ThreadTest-${taskSeq.incrementAndGet()}")
                t.isDaemon = false
                t.priority = Thread.NORM_PRIORITY
                t
            },
            { r, e ->
                ThreadPoolExecutor.CallerRunsPolicy().rejectedExecution(r, e)
            },
        )
    }

    fun test() {
        Log.d(TAG, "test() caller=${Thread.currentThread().name}")

        // 同步：当前线程立刻跑完才返回（MainActivity 调 test() 时是主线程，任务不能 sleep / 发网）
        execute(newTask("execute", blockMs = 0))
        Log.d(TAG, "execute() 已返回，说明调用线程被阻塞到任务结束")

        // 异步：丢给线程池，调用线程立刻返回。对照 RealCall.enqueue() → Dispatcher.enqueue()
        enqueue(newTask("enqueue-1"))
        enqueue(newTask("enqueue-2"))
        enqueue(newTask("enqueue-3"))
        Log.d(TAG, "enqueue() 已返回，任务在池线程上跑 poolSize=${mExecutor.poolSize}")
    }

    /**
     * 同步执行：当前线程直接 `run()`，对照 `RealCall.execute()`。
     * Dispatcher.executed() 只记账，不切线程。
     */
    fun execute(task: Runnable) {
        Log.d(TAG, "execute() submit on ${Thread.currentThread().name}")
        task.run()
    }

    /**
     * 异步执行：提交到线程池，对照 `RealCall.enqueue()` → `Dispatcher.enqueue()` →
     * `executorService.execute(AsyncCall)`。
     */
    fun enqueue(task: Runnable) {
        Log.d(TAG, "enqueue() submit on ${Thread.currentThread().name}")
        mExecutor.execute(task)
    }

    private fun newTask(kind: String, blockMs: Long = 200L): Runnable {
        return Runnable {
            val name = Thread.currentThread().name
            Log.d(TAG, "$kind START on $name")
            if (blockMs > 0) {
                Thread.sleep(blockMs)
            }
            Log.d(TAG, "$kind END on $name")
        }
    }
}
