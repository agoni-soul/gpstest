package com.haha.servicerouter.interfaces

import android.content.Context
import com.haha.servicerouter.core.DOFRouter

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
interface IRouteInterceptor {
    fun intercept(context: Context, navigator: DOFRouter.Navigator): Boolean
}