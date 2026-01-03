package com.haha.servicerouter.matcher

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
object DefaultMatcher: PathMatcher {

    override fun match(path: String, path2: String): Boolean {
        return path == path2
    }
}