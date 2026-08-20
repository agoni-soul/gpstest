package com.haha.main

import android.content.Context
import android.view.View
import com.haha.main.collection.CollectionTest
import com.haha.main.handler.HandlerTest
import com.haha.main.leakCanary.LeakCanaryTest
import com.haha.main.plugin.PluginTest
import com.haha.main.retrofit.RetrofitTest
import com.haha.main.service.ServiceTest
import com.haha.main.thread.ThreadTest
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