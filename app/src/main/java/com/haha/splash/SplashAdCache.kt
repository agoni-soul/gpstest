package com.haha.splash

import android.content.Context
import com.haha.hahalearn.R

/**
 * 开屏本地缓存。首次用内置物料种子，展示后刷新过期时间，模拟闲时预加载下一条。
 */
class SplashAdCache(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun getReadyAd(): SplashAd? {
        if (SplashAdSession.dismissed) {
            return null
        }
        seedDefaultIfEmpty()
        val expireAt = prefs.getLong(KEY_EXPIRE, 0L)
        if (expireAt < System.currentTimeMillis()) {
            return null
        }
        val lastShownAt = prefs.getLong(KEY_LAST_SHOWN, 0L)
        if (lastShownAt > 0L && System.currentTimeMillis() - lastShownAt < MIN_INTERVAL_MS) {
            return null
        }
        return SplashAd(
            id = prefs.getString(KEY_ID, DEFAULT_ID) ?: DEFAULT_ID,
            imageRes = DEFAULT_IMAGE_RES,
            landingUrl = prefs.getString(KEY_URL, DEFAULT_LANDING) ?: DEFAULT_LANDING,
            durationMs = prefs.getLong(KEY_DURATION, DEFAULT_DURATION_MS),
            expireAt = expireAt,
        )
    }

    fun markShown() {
        prefs.edit().putLong(KEY_LAST_SHOWN, System.currentTimeMillis()).apply()
    }

    /** 首页闲时 / 开屏关闭后预加载下一条（Demo 刷新 TTL，保持内置图 + 站内落地 URL）。 */
    fun preloadNext() {
        writeDefault(force = true)
    }

    private fun seedDefaultIfEmpty() {
        if (!prefs.contains(KEY_ID)) {
            writeDefault(force = true)
        }
    }

    private fun writeDefault(force: Boolean) {
        if (!force && prefs.contains(KEY_ID)) {
            return
        }
        prefs.edit()
            .putString(KEY_ID, DEFAULT_ID)
            .putString(KEY_URL, DEFAULT_LANDING)
            .putLong(KEY_DURATION, DEFAULT_DURATION_MS)
            .putLong(KEY_EXPIRE, System.currentTimeMillis() + DEFAULT_TTL_MS)
            .apply()
    }

    companion object {
        private const val PREF = "splash_ad_cache"
        private const val KEY_ID = "id"
        private const val KEY_URL = "landing_url"
        private const val KEY_EXPIRE = "expire_at"
        private const val KEY_DURATION = "duration_ms"
        private const val KEY_LAST_SHOWN = "last_shown_at"

        const val DEFAULT_ID = "builtin_baidu"
        const val DEFAULT_LANDING = "https://www.baidu.com"
        const val DEFAULT_DURATION_MS = 5_000L
        const val DEFAULT_TTL_MS = 7L * 24 * 60 * 60 * 1000

        /**
         * 淘宝常见冷启动间隔约 4h。学习项目默认 0，保证每次冷启动都能走到开屏。
         */
        const val MIN_INTERVAL_MS = 0L
        val DEFAULT_IMAGE_RES = R.drawable.bg_splash_ad
    }
}
