package com.soul.main

import android.content.Context
import android.view.View
import com.soul.main.collection.CollectionTest
import com.soul.main.handler.HandlerTest
import com.soul.main.leakCanary.LeakCanaryTest
import com.soul.main.plugin.PluginTest
import com.soul.main.retrofit.RetrofitTest
import com.soul.main.service.ServiceTest
import com.soul.main.thread.ThreadTest
import java.util.Random

/**
 *
 * @author:     haha
 * @date:       2025/7/31
 * Description: 测试学习的工具类
 *
 **/
object TestLearnUtils {

    private val retrofitTest: RetrofitTest by lazy(LazyThreadSafetyMode.PUBLICATION) {
        RetrofitTest()
    }

    private val threadTest: ThreadTest by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ThreadTest()
    }

    private val collectionTest: CollectionTest by lazy(LazyThreadSafetyMode.NONE) {
        CollectionTest()
    }

    fun test(context: Context) {
        SharedPreference.test(context)
        ServiceTest.test(context)
        PluginTest.test(context)
        LeakCanaryTest.main()
    }

    fun test(view: View) {
        HandlerTest.handlerLoop(view)
    }

    fun test() {
        retrofitTest.test()
        threadTest.test()
        collectionTest.test()
        mapTest()
    }

    private fun mapTest() {
        val map: MutableMap<Int, String> = HashMap()
        val random = Random()
        for (i in 0..9) {
            map[random.nextInt(100)] = i.toString()
        }
        map.forEach { (string, i) ->
            println("$string: $i")
        }
        val sortedMap = map.toSortedMap { o1, o2 ->
            o1 - o2
        }
        println("sorted")
        sortedMap.forEach { (string, i) ->
            println("$string: $i")
        }
    }

    fun destroy() {
        HandlerTest.destroy()
    }
}