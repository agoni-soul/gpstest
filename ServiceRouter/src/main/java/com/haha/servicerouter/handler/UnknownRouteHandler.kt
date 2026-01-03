package com.haha.servicerouter.handler

import android.content.Context
import com.haha.servicerouter.core.DOFRouter
import com.haha.servicerouterannotation.annotation.data.RouteMetaData
import com.haha.servicerouterutils.utils.Logger

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
internal class UnknownRouteHandler(routeMetaData: RouteMetaData): RouteHandler(routeMetaData) {
    override fun handle(context: Context, navigator: DOFRouter.Navigator) {
        Logger.w("Unknown route : ${routeMetadata.clazz.name}")
    }
}