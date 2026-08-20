package com.haha.dynamicTextView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import com.haha.util.DpOrSpToPxTransfer

/**
 * @auther: haha
 * @Date:   2026/1/18
 * @Detail:
 */
class DynamicView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {

    private var mOriginalColor = Color.RED

    private var mTouchColor = Color.GRAY

    private var touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    // 平滑移动参数
    private val maxMovePerFrame = DpOrSpToPxTransfer.dp2px(context, 50) // 每帧最大移动距离
    private val springBackDuration = 200L // 反弹动画时长

    private var mColor = mOriginalColor
    private var mParentWidth = 0
    private var mParentHeight = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = mColor
    }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 确保父布局已经完成测量
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
        // 获取父容器
        parent?.let {
            mParentWidth = it.width
            mParentHeight = it.height
        }
    }

    private var mLastX = 0f
    private var mLastY = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var isTouchEvent = super.onTouchEvent(event)
        println("event: (x, y) = (${event?.x}, ${event?.y}), event: (rawx, rawy) = (${event?.rawX}, ${event?.rawY}), action = ${event?.action}")
        println("l = $left, top = $top, right = $right, bottom = $bottom, x = $x, y = $y")

        if (event == null) return isTouchEvent
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                paint.color = mTouchColor
                mLastX = event.rawX
                mLastY = event.rawY
                invalidate()
                isTouchEvent = true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX.toInt() - mLastX
                val dy = event.rawY.toInt() - mLastY
                // 限制单次最大移动距离，避免快速拖动时超出边界
                val limitedDx = dx.coerceIn(-maxMovePerFrame.toFloat(), maxMovePerFrame.toFloat())
                val limitedDy = dy.coerceIn(-maxMovePerFrame.toFloat(), maxMovePerFrame.toFloat())
                // 计算新位置时直接进行边界检查
                calculateAndSetPosition(limitedDx, limitedDy)
                mLastX = event.rawX
                mLastY = event.rawY
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                paint.color = mOriginalColor
                mLastX = 0f
                mLastY = 0f
                ensureWithinBounds()
                invalidate()
            }
        }
        return isTouchEvent
    }

    private fun calculateAndSetPosition(dx: Float, dy: Float) {
        // 方法1：逐步逼近边界
        var targetX = x + dx
        var targetY = y + dy

        // 逐步检查，防止一次移动过多
        val steps = 5 // 分成5步移动
        val stepX = dx / steps
        val stepY = dy / steps

        for (i in 1..steps) {
            val testX = x + stepX * i
            val testY = y + stepY * i

            // 检查每一步是否都在边界内
            if (isPositionValid(testX, testY)) {
                targetX = testX
                targetY = testY
            } else {
                // 如果某一步超出边界，就停在边界处
                targetX = getValidX(testX)
                targetY = getValidY(testY)
                break
            }
        }
        x = targetX
        y = targetY
    }

    private fun ensureWithinBounds() {
        var needsAnimation = false
        var targetX = x
        var targetY = y

        // 检查并修正X坐标
        if (x < 0) {
            targetX = 0f
            needsAnimation = true
        } else if (x > mParentWidth - width) {
            targetX = (mParentWidth - width).toFloat()
            needsAnimation = true
        }

        // 检查并修正Y坐标
        if (y < 0) {
            targetY = 0f
            needsAnimation = true
        } else if (y > mParentHeight - height) {
            targetY = (mParentHeight - height).toFloat()
            needsAnimation = true
        }

        // 如果有超出边界，执行弹性动画
        if (needsAnimation) {
            animate()
                .x(targetX)
                .y(targetY)
                .setDuration(springBackDuration)
                .setInterpolator(OvershootInterpolator(0.5f))
                .start()
        }
        x = targetX
        y = targetY
    }

    private fun isPositionValid(x: Float, y: Float): Boolean {
        return x >= 0 &&
                y >= 0 &&
                x <= (mParentWidth - width) &&
                y <= (mParentHeight - height)
    }

    private fun getValidX(x: Float): Float {
        return x.coerceIn(0f, (mParentWidth - width).toFloat())
    }

    private fun getValidY(y: Float): Float {
        return y.coerceIn(0f, (mParentHeight - height).toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        println("onDraw")
        super.onDraw(canvas)
        canvas.drawPaint(paint)
    }
}