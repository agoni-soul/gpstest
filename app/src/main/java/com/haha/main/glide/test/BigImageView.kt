package com.haha.main.glide.test

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import java.io.File
import java.io.FileInputStream
import kotlin.math.min

/**
 * @auther: haha
 * @Date:   2026/1/22
 * @Detail:
 */
class BigImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var bitmapRegionDecoder: BitmapRegionDecoder? = null
    private var mImageWidth = 0
    private var mImageHeight = 0
    private var mSampleSize = 1
    private val rect = Rect()
    private var currentBitmap: Bitmap? = null
    private val matrix = Matrix()
    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, GestureListener())
        setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    // 4.1 加载大图
    fun setBigImage(file: File) {
        Thread {
            try {
                val inputStream = FileInputStream(file)
                val channel = inputStream.channel
                bitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream, false)

                mImageWidth = bitmapRegionDecoder!!.width
                mImageHeight = bitmapRegionDecoder!!.height

                // 计算初始采样率
                val viewWidth = width
                val viewHeight = height
                mSampleSize = calculateInitialSampleSize(
                    mImageWidth,
                    mImageHeight,
                    viewWidth,
                    viewHeight
                )

                // 加载首屏
                loadVisibleRegion()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun calculateInitialSampleSize(
        imageWidth: Int, imageHeight: Int, viewWidth: Int, viewHeight: Int
    ): Int {
        var sampleSize = 1
        return sampleSize
    }


    // 4.2 加载可见区域
    private fun loadVisibleRegion() {
        bitmapRegionDecoder?.let { decoder ->
            // 计算可见区域
            rect.set(
                0,
                0,
                min(mImageWidth, width * mSampleSize),
                min(mImageHeight, height * mSampleSize)
            )

            val options = BitmapFactory.Options().apply {
                inSampleSize = mSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            // 解码区域
            val bitmap = decoder.decodeRegion(rect, options)

            // 在主线程更新
            post {
                currentBitmap?.recycle()
                currentBitmap = bitmap
                invalidate()
            }
        }
    }

    // 4.3 绘制
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        currentBitmap?.let { bitmap ->
            // 计算缩放和平移
            val scale = width.toFloat() / bitmap.width
            matrix.reset()
            matrix.postScale(scale, scale)

            canvas.drawBitmap(bitmap, matrix, null)
        }
    }

    // 4.4 手势处理
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // 处理滑动，更新显示区域
            rect.offset(distanceX.toInt() * mSampleSize, distanceY.toInt() * mSampleSize)
            loadVisibleRegion()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // 双击放大/缩小
            mSampleSize = if (mSampleSize > 1) 1 else 4
            loadVisibleRegion()
            return true
        }
    }

    // 4.5 释放资源
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bitmapRegionDecoder?.recycle()
        currentBitmap?.recycle()
    }
}