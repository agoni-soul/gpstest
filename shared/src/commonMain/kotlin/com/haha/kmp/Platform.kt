package com.haha.kmp

/**
 * KMP 核心能力：common 里只声明「期望」，各平台自己提供「实现」。
 *
 * - Android → [androidMain] `Platform.android.kt`
 * - iOS → [iosMain] `Platform.ios.kt`
 *
 * 业务代码只依赖本文件，不要直接 import android.* / UIKit。
 */
interface Platform {
    /** 短名称，例如 Android / iOS */
    val name: String

    /** 更细的系统信息，便于对照 expect/actual */
    val details: String
}

expect fun getPlatform(): Platform
