package com.soul.main.glide.test

import android.graphics.Bitmap

/**
 * @auther: haha
 * @Date:   2026/1/22
 * @Detail:
 */
//class BigImageLoader {
//
//    private val counter = AtomicLong(0L)
//
//    private val loadingTasks = ConcurrentHashMap<String, Runnable>()
//
//    // 2.1 使用LruCache + 弱引用
//    private val memoryCache: LruCache<String, Bitmap> by lazy {
//        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
//        val cacheSize = maxMemory / 8 // 使用1/8的内存
//
//        object : LruCache<String, Bitmap>(cacheSize) {
//            override fun sizeOf(key: String, bitmap: Bitmap): Int {
//                // Android 4.0+ 使用bitmap.allocationByteCount
//                return bitmap.allocationByteCount / 1024
//            }
//
//            override fun entryRemoved(
//                evicted: Boolean,
//                key: String,
//                oldValue: Bitmap,
//                newValue: Bitmap?
//            ) {
//                // 可以转移到弱引用缓存
//                weakCache[key] = WeakReference(oldValue)
//            }
//        }
//    }
//
//    // 2.2 弱引用缓存作为二级内存缓存
//    private val weakCache = Collections.synchronizedMap(
//        HashMap<String, WeakReference<Bitmap>>()
//    )
//
//    // 2.3 大图特殊处理：只缓存缩略图
//    fun cacheBigImage(url: String, bitmap: Bitmap, isBigImage: Boolean) {
//        if (isBigImage) {
//            // 大图只缓存缩略版本
//            val thumbnail = createThumbnail(bitmap)
//            memoryCache.put("${url}_thumbnail", thumbnail)
//            bitmap.recycle()
//        } else {
//            memoryCache.put(url, bitmap)
//        }
//    }
//
//    // 2.4 创建缩略图
//    private fun createThumbnail(bitmap: Bitmap): Bitmap {
//        val maxSize = 1024 // 缩略图最大尺寸
//        val width = bitmap.width
//        val height = bitmap.height
//
//        return if (width > maxSize || height > maxSize) {
//            val scale = maxSize.toFloat() / maxOf(width, height)
//            val newWidth = (width * scale).toInt()
//            val newHeight = (height * scale).toInt()
//
////            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
//            bitmap.scale(newWidth, newHeight)
//        } else {
//            bitmap
//        }
//    }
//
//    // 5.1 加载状态管理
//    sealed class LoadStatus {
//        object Loading : LoadStatus()
//        data class Success(val bitmap: Bitmap) : LoadStatus()
//        data class Error(val exception: Exception) : LoadStatus()
//    }
//
//    // 5.2 加载任务
//    inner class LoadTask(
//        private val url: String,
//        private val target: ImageTarget
//    ) : Runnable {
//
//        override fun run() {
//            try {
//                // 5.2.1 检查内存缓存
//                var bitmap = memoryCache.get(url)
//                if (bitmap != null) {
//                    target.onSuccess(bitmap)
//                    return
//                }
//
//                // 5.2.2 检查磁盘缓存
//                bitmap = loadFromDiskCache(url)
//                if (bitmap != null) {
//                    memoryCache.put(url, bitmap)
//                    target.onSuccess(bitmap)
//                    return
//                }
//
//                // 5.2.3 网络下载
//                val file = downloadFromNetwork(url)
//
//                // 5.2.4 判断是否为大图
//                val fileSize = file.length()
//                val isBigImage = fileSize > 10 * 1024 * 1024
//
//                if (isBigImage) {
//                    // 大图特殊处理：只解码缩略图
//                    val thumbnail = decodeThumbnail(file)
//                    memoryCache.put("${url}_thumbnail", thumbnail)
//                    target.onSuccess(thumbnail)
//
//                    // 异步缓存原图
//                    cacheOriginalImage(file, url)
//                } else {
//                    // 普通图片
//                    bitmap = decodeNormalImage(file)
//                    memoryCache.put(url, bitmap)
//                    target.onSuccess(bitmap)
//                }
//            } catch (e: Exception) {
//                target.onError(e)
//            }
//        }
//
//        private fun decodeThumbnail(file: File): Bitmap {
//            val options = BitmapFactory.Options().apply {
//                inSampleSize = calculateThumbnailSampleSize(file)
//                inPreferredConfig = Bitmap.Config.RGB_565
//            }
//            return BitmapFactory.decodeFile(file.absolutePath, options)!!
//        }
//
//        private fun cacheOriginalImage(file: File, url: String) {
//            // 将原图存入磁盘缓存
//            diskCache.put(url, file)
//        }
//    }
//
//    // 5.3 线程池管理
//    private val executor: ExecutorService = ThreadPoolExecutor(
//        0, // 核心线程数
//        5, // 最大线程数
//        60L, TimeUnit.SECONDS, // 空闲线程存活时间
//        LinkedBlockingQueue<Runnable>()
//    ) { r ->
//        Thread(r, "BigImageLoader-${counter.getAndIncrement()}")
//    }
//
//    // 5.4 加载方法
//    fun load(url: String, target: ImageTarget) {
//        // 5.4.1 检查是否正在加载
//        if (loadingTasks.containsKey(url)) {
//            return
//        }
//
//        // 5.4.2 创建并执行任务
//        val task = LoadTask(url, target)
//        loadingTasks[url] = task
//        executor.execute(task)
//    }
//
//    // 3.2 智能采样率计算
//    fun calculateInSampleSize(
//        options: BitmapFactory.Options,
//        reqWidth: Int,
//        reqHeight: Int
//    ): Int {
//        val height = options.outHeight
//        val width = options.outWidth
//        var inSampleSize = 1
//
//        if (height > reqHeight || width > reqWidth) {
//            val halfHeight = height / 2
//            val halfWidth = width / 2
//
//            // 计算最大采样率，使图片尺寸接近要求
//            while ((halfHeight / inSampleSize) >= reqHeight &&
//                (halfWidth / inSampleSize) >= reqWidth) {
//                inSampleSize *= 2
//            }
//        }
//
//        return inSampleSize
//    }
//}

interface ImageTarget {
    fun onSuccess(bitmap: Bitmap)

    fun onProcess(process: Int)

    fun onError(e: Exception)
}