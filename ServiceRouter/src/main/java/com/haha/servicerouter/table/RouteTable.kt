package com.haha.servicerouter.table

import com.haha.servicerouter.matcher.DefaultMatcher
import com.haha.servicerouter.matcher.PathMatcher
import com.haha.servicerouterannotation.annotation.data.InterceptorMetaData
import com.haha.servicerouterannotation.annotation.data.RouteMetaData
import java.util.TreeMap

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
internal object RouteTable {

    internal val routes = HashMap<String, RouteMetaData>()
    internal val matchers = mutableListOf<PathMatcher>(DefaultMatcher)
    internal val interceptors = TreeMap<Int, InterceptorMetaData>()

    fun clear() {
        routes.clear()
        matchers.clear()
        interceptors.clear()
    }
}