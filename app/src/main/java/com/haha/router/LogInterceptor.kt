package com.haha.router

import android.content.Context
import android.util.Log
import com.haha.servicerouter.core.DOFRouter
import com.haha.servicerouter.interfaces.IRouteInterceptor
import com.haha.servicerouterannotation.annotation.Interceptor

/**
 * 演示拦截器：只打日志，不拦截跳转。
 */
@Interceptor(priority = 0, name = "LogInterceptor")
class LogInterceptor : IRouteInterceptor {
    override fun intercept(context: Context, navigator: DOFRouter.Navigator): Boolean {
        Log.d(TAG, "navigate path=${navigator.path}")
        return false
    }

    companion object {
        private const val TAG = "DOFRouter"
    }
}
