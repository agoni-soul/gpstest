package com.soul.main.glide.test

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * @auther: haha
 * @Date:   2026/1/22
 * @Detail:
 */
class BigImageDecoder {

    // 3.1 使用BitmapRegionDecoder分块加载
    fun decodeRegion(file: File, rect: Rect, sampleSize: Int): Bitmap? {
        return try {
            val inputStream = FileInputStream(file)
            val regionDecoder = BitmapRegionDecoder.newInstance(
                inputStream,
                false
            )

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // 减少内存
                inTempStorage = ByteArray(16 * 1024) // 16KB解码缓冲区
            }

            val bitmap = regionDecoder?.decodeRegion(rect, options)
            regionDecoder?.recycle()
            inputStream.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 3.2 智能采样率计算
    fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // 计算最大采样率，使图片尺寸接近要求
            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    // 3.3 流式解码（避免OOM）
    fun decodeStreamSafely(
        inputStream: InputStream,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap? {
        return try {
            // 先获取图片尺寸
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.reset()

            // 计算采样率
            options.inSampleSize = calculateInSampleSize(
                options,
                targetWidth,
                targetHeight
            )
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565
            options.inMutable = true

            // 使用inBitmap复用（API 11+）
//            val reusableBitmap = getReusableBitmap(options)
//            reusableBitmap?.let {
//                options.inBitmap = it
//            }

            BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: OutOfMemoryError) {
            System.gc()
            null
        }
    }
}