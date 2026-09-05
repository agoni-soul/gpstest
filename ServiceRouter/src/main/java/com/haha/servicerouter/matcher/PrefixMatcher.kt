package com.haha.servicerouter.matcher

import com.haha.servicerouterannotation.annotation.data.RouteMetaData

/**
 * 匹配 @Route.pathPrefix
 */
object PrefixMatcher : PathMatcher {

    override fun match(routePath: String, requestPath: String, meta: RouteMetaData): Boolean {
        val prefix = meta.pathPrefix
        return prefix.isNotEmpty() && requestPath.startsWith(prefix)
    }
}
