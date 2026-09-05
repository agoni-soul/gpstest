package com.haha.servicerouter.matcher

import com.haha.servicerouterannotation.annotation.data.RouteMetaData

/**
 * 匹配 @Route.pathPattern（正则）
 */
object PatternMatcher : PathMatcher {

    override fun match(routePath: String, requestPath: String, meta: RouteMetaData): Boolean {
        val pattern = meta.pathPattern
        if (pattern.isEmpty()) {
            return false
        }
        return try {
            requestPath.matches(pattern.toRegex())
        } catch (e: Exception) {
            false
        }
    }
}
