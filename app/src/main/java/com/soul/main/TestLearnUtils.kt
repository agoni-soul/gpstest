package com.soul.main

import android.content.Context
import android.view.View
import com.soul.main.handler.HandlerTest
import com.soul.main.plugin.PluginTest
import com.soul.main.retrofit.RetrofitTest
import com.soul.main.service.ServiceTest

/**
 *
 * @author:     haha
 * @date:       2025/7/31
 * Description: 测试学习的工具类
 *
 **/
object TestLearnUtils {

    private var retrofitTest: RetrofitTest? = null

    fun test(context: Context) {
        SharedPreference.test(context)
        ServiceTest.test(context)
        PluginTest.test(context)
    }

    fun test(view: View) {
        HandlerTest.handlerLoop(view)
    }

    fun test() {
        if (retrofitTest == null) {
            retrofitTest = RetrofitTest()
        }
        retrofitTest!!.test()
    }
}