package com.haha.image

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.haha.hahalearn.BuildConfig
import com.haha.network.HttpClient
import java.io.InputStream

/**
 * Glide 全局配置：内存/磁盘缓存 + 复用 [HttpClient] 的 OkHttp。
 * 由 [com.haha.startup.task.GlideInitTask] 触发 [Glide.get] 时装载。
 */
@GlideModule
class HahaGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val calculator = MemorySizeCalculator.Builder(context).build()
        builder.setMemoryCache(LruResourceCache(calculator.memoryCacheSize.toLong()))
        builder.setBitmapPool(LruBitmapPool(calculator.bitmapPoolSize.toLong()))
        builder.setDiskCache(
            InternalCacheDiskCacheFactory(context, "glide", 250L * 1024 * 1024)
        )
        builder.setLogLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val factory = if (HttpClient.isInitialized()) {
            OkHttpUrlLoader.Factory(HttpClient.okHttpClient)
        } else {
            OkHttpUrlLoader.Factory()
        }
        registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }

    override fun isManifestParsingEnabled(): Boolean = false
}
