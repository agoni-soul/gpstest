package com.haha.servicerouter.table

import com.haha.servicerouter.matcher.DefaultMatcher
import com.haha.servicerouter.matcher.PathMatcher
import com.haha.servicerouter.matcher.PatternMatcher
import com.haha.servicerouter.matcher.PrefixMatcher
import com.haha.servicerouter.table.RouteTable.rebuildIndex
import com.haha.servicerouterannotation.annotation.data.InterceptorMetaData
import com.haha.servicerouterannotation.annotation.data.RouteMetaData

/**
 * 运行时路由 / 拦截器表。
 * 精确 path 走 HashMap；前缀按最长匹配；正则在 [rebuildIndex] 时编译一次。
 */
internal object RouteTable {

    internal val routes = HashMap<String, RouteMetaData>()
    internal val matchers =
        mutableListOf<PathMatcher>(DefaultMatcher, PrefixMatcher, PatternMatcher)
    internal val interceptors = mutableListOf<InterceptorMetaData>()

    private val exactRoutes = HashMap<String, RouteMetaData>()
    private val prefixRoutes = mutableListOf<RouteMetaData>()
    private val patternRoutes = mutableListOf<CompiledPattern>()

    /**
     * 装表结束后重建索引。寻址顺序：精确 > 最长前缀 > 正则。
     */
    fun rebuildIndex() {
        exactRoutes.clear()
        prefixRoutes.clear()
        patternRoutes.clear()
        routes.values.forEach { meta ->
            when {
                meta.path.isNotEmpty() -> exactRoutes[meta.path] = meta
                meta.pathPrefix.isNotEmpty() -> prefixRoutes.add(meta)
                meta.pathPattern.isNotEmpty() -> {
                    try {
                        patternRoutes.add(CompiledPattern(meta.pathPattern.toRegex(), meta))
                    } catch (_: Exception) {
                        // 非法正则在编译期应被拦下，运行时跳过以免拖垮寻址
                    }
                }
            }
        }
        prefixRoutes.sortByDescending { it.pathPrefix.length }
        patternRoutes.sortBy { it.meta.priority }
    }

    /**
     * 只返回一条命中。精确 path 为 O(1)，前缀 / 正则只扫各自的小表。
     */
    fun find(requestPath: String): RouteMetaData? {
        exactRoutes[requestPath]?.let { return it }
        prefixRoutes.firstOrNull { requestPath.startsWith(it.pathPrefix) }?.let { return it }
        patternRoutes.firstOrNull { it.regex.matches(requestPath) }?.let { return it.meta }
        return null
    }

    fun clear() {
        routes.clear()
        interceptors.clear()
        exactRoutes.clear()
        prefixRoutes.clear()
        patternRoutes.clear()
        matchers.clear()
        matchers.add(DefaultMatcher)
        matchers.add(PrefixMatcher)
        matchers.add(PatternMatcher)
    }

    private data class CompiledPattern(
        val regex: Regex,
        val meta: RouteMetaData
    )
}