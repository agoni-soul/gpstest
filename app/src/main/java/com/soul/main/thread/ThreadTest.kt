package com.soul.main.thread

import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @auther: soulagoni
 * @Date:   2025/8/16
 * @Detail: 线程池测试
 */
class ThreadTest {
    private final val TAG = this.javaClass.simpleName

    val mExecutor: ThreadPoolExecutor by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ThreadPoolExecutor(
            3, 5, 10, TimeUnit.MILLISECONDS, SynchronousQueue(),
            { r ->
                val t = Thread(r)
                t.name = "Thread_" + System.currentTimeMillis() + "_" + r.hashCode()
                if (t.isDaemon) t.isDaemon = false
                if (t.priority != Thread.NORM_PRIORITY) t.priority = Thread.NORM_PRIORITY
                t
            }, { r, e ->
                throw RejectedExecutionException("Task $r rejected from $e")
            })
    }
    var mCount = 1

    fun test() {
        val r = Runnable {
            println("haha-${mCount++}")
        }
        mExecutor.execute(r)
    }
}