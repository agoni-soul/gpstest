package com.soul.main.leakCanary

import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference


/**
 * @auther: haha
 * @Date:   2025/12/23
 * @Detail:
 */
class LeakCanaryTest {


    companion object {
        @Volatile
        private var instance: LeakCanaryTest? = null

        private fun getInstance(): LeakCanaryTest {
            if (instance == null) {
                synchronized(LeakCanaryTest::class.java) {
                    if (instance == null) {
                        instance = LeakCanaryTest()
                    }
                }
            }
            return instance!!
        }

        fun main() {
            getInstance().test()
        }
    }

    private constructor()

    // 创建一个引用队列
    var queue: ReferenceQueue<Any?> = ReferenceQueue<Any?>()

    private fun test() {
        // 创建一个对象
        var obj: Any? = Any()
        // 创建一个弱引用，并指向这个对象，并且将引用队列传递给弱引用
        val reference = WeakReference<Any?>(obj, queue)
        // 打印出这个弱引用，为了跟gc之后queue里面的对比证明是同一个
        println("这个弱引用是:$reference")
        // gc一次看看(什么都没)
        System.gc()
        // 打印队列(应该是空)
        printlnQueue("before")

        // 先设置obj为null，obj可以被回收了
        obj = null
        // 再进行gc，此时obj应该被回收了，那么queue里面应该有这个弱引用了
        System.gc()
        // 再打印队列
        printlnQueue("after")
    }

    private fun printlnQueue(tag: String?) {
        print(tag)
        var obj: Any?
        // 循环打印引用队列
        while ((queue.poll().also { obj = it }) != null) {
            println(": $obj")
        }
        println()
    }

}