package com.haha.servicerouter.matcher

import com.haha.servicerouterannotation.annotation.data.RouteMetaData

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
interface PathMatcher {

    fun match(routePath: String, requestPath: String, meta: RouteMetaData): Boolean
}
