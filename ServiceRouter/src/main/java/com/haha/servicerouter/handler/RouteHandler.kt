package com.haha.servicerouter.handler

import android.content.Context
import com.haha.servicerouter.core.DOFRouter
import com.haha.servicerouter.exceptions.HandleException
import com.haha.servicerouterannotation.annotation.data.RouteMetaData

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
internal abstract class RouteHandler(val routeMetadata: RouteMetaData) {
    @Throws(HandleException::class)
    abstract fun handle(context: Context, navigator: DOFRouter.Navigator): Any?
}