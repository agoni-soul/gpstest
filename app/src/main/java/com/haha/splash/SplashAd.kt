package com.haha.splash

import androidx.annotation.DrawableRes

/**
 * 本地可展示的开屏物料。淘宝路径是「上次预加载、这次只读缓存」，
 * 没有未过期物料就不出，绝不在冷启动同步等网络。
 */
data class SplashAd(
    val id: String,
    @DrawableRes val imageRes: Int,
    val landingUrl: String,
    val durationMs: Long,
    val expireAt: Long,
)
