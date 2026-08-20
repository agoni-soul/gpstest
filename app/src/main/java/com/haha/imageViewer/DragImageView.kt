package com.haha.imageViewer

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatImageView

/**
 * @auther: haha
 * @Date:   2026/1/24
 * @Detail:
 */
class DragImageView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    AppCompatImageView(context, attrs, defStyleAttr) {

    private var mScaleGestureDetector: ScaleGestureDetector
    private var mGestureDetector: GestureDetector
    private var mIsScaling = false

    //负责图片的平移缩放
    private var mScaleMatrix: Matrix

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        scaleType = ScaleType.MATRIX
        mScaleGestureDetector = ScaleGestureDetector(context, ScaleGestureListener())
        mGestureDetector = GestureDetector(context, GestureListener())
        mScaleMatrix = Matrix()
    }

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var initialDistance = 0f
    private var lastFocusX = 0f
    private var lastFocusY = 0f

    private var mInitScale: Float = 0f
    private var mMinScale: Float = 0.1f
    private var mMaxScale: Float = 5f

    private var totalScale = 1f
    private var mLastPivotY = 1f
    private var mLastPivotX = 1f

    private var mParentWidth = 0
    private var mParentHeight = 0

    private val mLayoutListener: ViewTreeObserver.OnGlobalLayoutListener =
        ViewTreeObserver.OnGlobalLayoutListener {
            if (drawable == null) return@OnGlobalLayoutListener
            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight

            val scale: Float =
                (width * 1f / drawableWidth).coerceAtMost(height * 1f / drawableHeight)
            mInitScale = scale
            mMinScale = scale
            mMaxScale = scale * 5
            totalScale = scale
            val dx = width / 2 - drawableWidth / 2
            val dy = height / 2 - drawableHeight / 2
            mScaleMatrix.postTranslate(dx.toFloat(), dy.toFloat())
            mScaleMatrix.postScale(mInitScale, mInitScale, width / 2f, height / 2f)
            viewTreeObserver.removeOnGlobalLayoutListener(mLayoutListener)
        }

    private val mTouchListener = OnTouchListener { v, event ->
        var handled = mScaleGestureDetector.onTouchEvent(event)

        handled = mGestureDetector.onTouchEvent(event) || handled
        return@OnTouchListener true
    }

    private fun calculateDistance(event: MotionEvent): Float {
        var distance = 0f
        return distance
    }

    private fun calculateFocusX(event: MotionEvent): Float {
        var sum = 0f
        for (i in 0 until event.pointerCount) {
            sum += event.getX(i)
        }
        return sum / event.pointerCount
    }

    private fun calculateFocusY(event: MotionEvent): Float {
        var sum = 0f
        for (i in 0 until event.pointerCount) {
            sum += event.getY(i)
        }
        return sum / event.pointerCount
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val handled = super.onTouchEvent(event)
        if (event == null) return handled
        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    // 双指按下，记录初始距离和中心点
                    initialDistance = calculateDistance(event)
                    lastFocusX = calculateFocusX(event)
                    lastFocusY = calculateFocusY(event)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount >= 2) {
                    val currentDistance = calculateDistance(event)
                    val scaleDelta = currentDistance / initialDistance

                    // 应用增量缩放，锚定在上次中心点
                    this.scaleX = totalScale * mScaleGestureDetector.scaleFactor;
                    this.scaleY = totalScale * mScaleGestureDetector.scaleFactor;

                    // 同步调整pivot点以保持缩放中心稳定
                    this.pivotX = lastFocusX
                    this.pivotY = lastFocusY
                } else {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    // 触发拖动逻辑
//                    handleDrag(dx, dy)
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
            }
        }
        if (action == MotionEvent.ACTION_POINTER_DOWN && event.pointerCount == 2) {
            mScaleGestureDetector.onTouchEvent(event)
            return true
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            mIsScaling = false
        }

        if (mIsScaling) {
            return mScaleGestureDetector.onTouchEvent(event)
        } else {
            return mGestureDetector.onTouchEvent(event)
        }
    }

    private fun getScaleFactor(): Float {
        val values = FloatArray(9)
        mScaleMatrix.getValues(values)
        return values[Matrix.MSCALE_X]
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(mLayoutListener)
        setOnTouchListener(mTouchListener)
        post {
            updateParentInfo()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateParentInfo()
    }

    private fun updateParentInfo() {
        val parent = parent as? ViewGroup
        parent?.let {
            mParentWidth = it.width
            mParentHeight = it.height
        }
    }

    private fun setScaleFactor(scaleFactor: Float) {
        scaleX = scaleFactor
        scaleY = scaleFactor
    }

    private inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            println("onScaleBegin")
            // 获取缩放中心点（两个手指的中心）
            mLastPivotX = detector.focusX
            mLastPivotY = detector.focusY

            // 设置缩放中心
            pivotX = mLastPivotX
            pivotY = mLastPivotY
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (drawable == null) return true
            var scaleFactor = detector.scaleFactor
            val scale = getScaleFactor()
            if (scale * scaleFactor < mMinScale) {
                scaleFactor = mMinScale / scale
            }
            if (scale * scaleFactor > mMaxScale) {
                scaleFactor = mMaxScale / scale
            }
            mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)

            checkBorderForScale()
            imageMatrix = mScaleMatrix
            return true
        }

        private fun adjustPosition(detector: ScaleGestureDetector, previousScale: Float) {
            // 计算缩放中心的变化
            val focusX = detector.focusX
            val focusY = detector.focusY

            // 调整位置，让缩放中心保持在手指位置
            val deltaX: Float = (focusX - mLastPivotX) * (1 - totalScale / previousScale)
            val deltaY: Float = (focusY - mLastPivotY) * (1 - totalScale / previousScale)

            translationX += deltaX
            translationY += deltaY
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            totalScale *= detector.scaleFactor
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onShowPress(e: MotionEvent) {
            println("onShowPrecess")
        }

        override fun onLongPress(e: MotionEvent) {
            println("保存照片")
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // 滑动时移动图片
            translationX -= distanceX
            translationY -= distanceY
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (totalScale < 2.5f) {
                totalScale = 2.5f
            } else if (totalScale < 5f) {
                totalScale = 5f
            } else {
                totalScale = 1f
            }
            setScaleFactor(totalScale)
            return true
        }
    }

    private fun checkBorderForScale() {
        val rectF = getMatrixRectF()
        var deltaX = 0f
        var deltaY = 0f

        if (rectF.width() >= width) {
            if (rectF.left > 0) { //和屏幕左边有空隙
                deltaX = -rectF.left //左边移动
            }
            // 和屏幕as
            if (rectF.right < width) {
                deltaX = width - rectF.right
            }
        }

        if (rectF.height() >= height) {
            if (rectF.top > 0) {
                deltaY = -rectF.top
            }

            if (rectF.bottom < height) {
                deltaY = height - rectF.bottom
            }
        }

        if (rectF.width() < width) {
            deltaX = width / 2 - rectF.right + rectF.width() / 2
        }
        if (rectF.height() < height) {
            deltaY = height / 2 - rectF.bottom + rectF.height() / 2
        }
        mScaleMatrix.postTranslate(deltaX, deltaY)
    }

    private fun getMatrixRectF(): RectF {
        val matrix = mScaleMatrix
        val rectF = RectF()
        if (drawable != null) {
            rectF.set(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
            matrix.mapRect(rectF)
        }
        return rectF
    }
}