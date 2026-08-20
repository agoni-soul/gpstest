package com.haha.main.glide.test

import android.content.Context
import com.bumptech.glide.disklrucache.DiskLruCache
import java.io.File

/**
 * @auther: haha
 * @Date:   2026/1/22
 * @Detail:
 */
class BigImageStorage {
    // 1.1 磁盘缓存策略
    companion object {
        private const val MAX_DISK_CACHE_SIZE = 500 * 1024 * 1024L // 500MB
        private const val BIG_IMAGE_THRESHOLD = 10 * 1024 * 1024L // 10MB以上视为大图
    }

    // 1.2 使用DiskLruCache
    private fun initDiskCache(context: Context): DiskLruCache {
        val cacheDir = File(context.cacheDir, "big_image_cache")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        return DiskLruCache.open(
            cacheDir,
            1,  // 版本号
            2,  // 每个key对应2个文件（原图和缩略信息）
            MAX_DISK_CACHE_SIZE
        )
    }

    // 1.3 大图分块存储
    data class ImageChunk(
        val index: Int,
        val data: ByteArray,
        val offset: Long,
        val size: Int
    )

    // 1.4 存储大图元数据
    data class BigImageMeta(
        val totalSize: Long,
        val width: Int,
        val height: Int,
        val mimeType: String,
        val chunkCount: Int,
        val chunkSize: Int = 1024 * 1024 // 1MB每块
    )
}