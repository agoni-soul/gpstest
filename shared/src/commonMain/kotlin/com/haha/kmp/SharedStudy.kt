package com.haha.kmp

/**
 * 纯 Kotlin 共享逻辑：不碰 UI、不碰平台 API。
 * Android / iOS / Desktop 都可以直接调用。
 */
object SharedStudy {

    fun greeting(platform: Platform = getPlatform()): String {
        return "Hello from KMP · 当前平台 ${platform.name}"
    }

    /** 教学用：一份算法，两端共用。n 过大时返回 -1，避免 Int 溢出演示时卡死。 */
    fun fibonacci(n: Int): Long {
        if (n < 0) return -1
        if (n > 92) return -1
        if (n <= 1) return n.toLong()
        var prev = 0L
        var curr = 1L
        repeat(n - 1) {
            val next = prev + curr
            prev = curr
            curr = next
        }
        return curr
    }
}
