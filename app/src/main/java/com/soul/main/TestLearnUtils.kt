package com.soul.main

import android.content.Context
import android.view.View
import com.soul.main.collection.CollectionTest
import com.soul.main.handler.HandlerTest
import com.soul.main.plugin.PluginTest
import com.soul.main.retrofit.RetrofitTest
import com.soul.main.service.ServiceTest
import com.soul.main.thread.ThreadTest

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
    }

    fun test(view: View) {
        HandlerTest.handlerLoop(view)
    }

    fun test() {
        retrofitTest.test()
        threadTest.test()
        collectionTest.test()
    }
}