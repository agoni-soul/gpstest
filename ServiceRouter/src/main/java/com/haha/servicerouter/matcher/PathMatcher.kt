package com.haha.servicerouter.matcher

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
interface PathMatcher {

    fun match(path: String, path2: String): Boolean
}
