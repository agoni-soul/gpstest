package com.haha.servicerouterannotation.annotation.data

import com.haha.servicerouterannotation.annotation.RouteType

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
data class RouteMetaData(
    val routeType: RouteType,
    val priority: Int,
    val name: String,
    val path: String,
    val pathPrefix: String,
    val pathPattern: String,
    val clazz: Class<*>
)