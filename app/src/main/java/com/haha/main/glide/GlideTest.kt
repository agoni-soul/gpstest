package com.haha.main.glide

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.haha.hahalearn.R

/**
 *
 * @author:     haha
 * @date:       2025/7/31
 * Description: Glide学习
 *
 **/
class GlideTest {
    fun test(view: ImageView) {
        Glide.with(view.context)
            .load(R.drawable.me_ic_message)
            .error(R.drawable.circle)
            .override(500, 500) // 固定尺寸
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(view)
        val glideBuilder = GlideBuilder()
            .setMemoryCache(LruResourceCache(10 * 1024 * 1024)) // 10MB 内存缓存
    }
}