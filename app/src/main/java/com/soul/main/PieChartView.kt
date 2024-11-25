package com.soul.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import com.soul.gpstest.R
import com.soul.util.DpOrSpToPxTransfer
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


/**
 *
 * @author : haha
 * @date   : 2024-11-22
 * @desc   : 扇形圆
 *
 */
class PieChartView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
    private val TAG = this.javaClass.simpleName

    companion object {
        const val DIRECTION_TOP = "top"
        const val DIRECTION_LEFT = "left"
        const val DIRECTION_BOTTOM = "bottom"
        const val DIRECTION_RIGHT = "right"
    }
    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private var piePaint: Paint? = null //扇形画笔
    private var outerLinePaint: Paint? = null //轮廓画笔
    private var circlePaint: Paint? = null
    private var trianglePaint: Paint? = null

    private var outerRadius = 0f //半径
    private var innerRadius = 0f
    private var triangleRadius = 0f
    private var triangleSide = 0f
    private var triangleHeight = 0f

    private val OUTER_LINE_WIDTH = 3f
    private val START_DEGREE = -45f //开始绘制角度

    //饼状图动画效果
    private var X = 0f
    private var Y = 0f
    private var rectF: RectF? = null

    private var mGradualDefaultColor: Int = Color.parseColor("#F9F9F9")
    private var mGradualStartColor: Int = Color.parseColor("#FDFDFD")
    private var mGradualEndColor: Int = Color.parseColor("#E5E5E8")
    private var mTriangleBgColor: Int = Color.parseColor("#C7C7CC")
    private var mInnerBgColor: Int = Color.parseColor("#F0F0F0")
    private var mIsTouch = false
    private var mTouchDownPaint: Paint? = null

    private var context: Context? = context
    private var list: MutableList<PieChartBean>? = null
    private var mCallback: ((Int, Boolean) -> Unit)? = null
    private var directionPosition: String? = null
    private var mPosition = 0

    init {
        innerRadius = DpOrSpToPxTransfer.dp2px(context, 24).toFloat()
        triangleRadius = DpOrSpToPxTransfer.dp2px(context, 78).toFloat()
        triangleSide = DpOrSpToPxTransfer.dp2px(context, 14).toFloat()
        triangleHeight = DpOrSpToPxTransfer.dp2px(context, 6).toFloat()

        piePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        mTouchDownPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        circlePaint =  Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = mInnerBgColor
        }

        trianglePaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            alpha = 255
            color = mTriangleBgColor
        }

        outerLinePaint = Paint().apply {
            isAntiAlias = true
            alpha = 255
            style = Paint.Style.STROKE
            strokeWidth = OUTER_LINE_WIDTH
            color = Color.RED
        }

        rectF = RectF()
    }

    fun setDirectionPosition(directionPosition: String) {
        this.directionPosition = directionPosition
        mPosition = when (directionPosition) {
            DIRECTION_TOP -> 3
            DIRECTION_RIGHT -> 0
            DIRECTION_BOTTOM -> 1
            DIRECTION_LEFT -> 2
            else -> 0
        }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        outerRadius = (min(measuredWidth, measuredHeight)).toFloat()

        when (mPosition) {
            0 -> {
                X = 0f
                Y = (measuredHeight / 2.0).toFloat()
                rectF.apply {
                    left =  0
                    top = (Y - outerRadius/2).toInt()
                    right = outerRadius.toInt()
                    bottom = (Y + outerRadius/2).toInt()
                }
            }
            1 -> {
                X = (measuredWidth / 2.0).toFloat()
                Y = 0f
                rectF.apply {
                    left =  (X - outerRadius/2).toInt()
                    top = 0
                    right = (X + outerRadius/2).toInt()
                    bottom = outerRadius.toInt()
                }
            }
            2 -> {
                X = 0f
                Y = (measuredHeight / 2.0).toFloat()
                rectF.apply {
                    left =  -1 * outerRadius.toInt()
                    top = (Y - outerRadius/2).toInt()
                    right = 0
                    bottom = (Y + outerRadius/2).toInt()
                }
            }
            3 -> {
                X = (measuredWidth / 2.0).toFloat()
                Y = 0f
                rectF.apply {
                    left =  (X - outerRadius/2).toInt()
                    top = -1 * outerRadius.toInt()
                    right = (X + outerRadius/2).toInt()
                    bottom = 0
                }
            }
            else -> {
                X = 0f
                Y = 0f
                rectF.apply {
                    left = 0
                    top = 0
                    right = outerRadius.toInt()
                    bottom = measuredHeight
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            Log.d(TAG, "onTouchEvent: event = null")
            return super.onTouchEvent(event)
        }
        val action = event.action
        val x = event.x
        val y = event.y
        val position: Int
        if (isRing(x, y)) {
            val angle = Math.toDegrees(atan2((y - Y).toDouble(), (x - X).toDouble()))
            Log.d(
                TAG,
                "angle = $angle, d = ${atan2((y - Y).toDouble(), (x - X).toDouble())}"
            )
            position = if (angle in -45.0..45.0) {
                0
            } else if (angle in 45.0..135.0) {
                1
            } else if (angle in -135.0..-45.0) {
                3
            } else {
                2
            }
            Log.d(TAG, "onTouchEvent: mPosition = ${mPosition}, position = $position, action = ${action}")
            if (position == mPosition) {
                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        mCallback?.invoke(position, true)
                        mIsTouch = true
                        Log.d(TAG, "onTouchEvent: mIsTouch = true")
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
//                      mCallback?.invoke(position, true)
                        Log.d(TAG, "onTouchEvent: mIsTouch = true")
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        mCallback?.invoke(position, false)
                        mIsTouch = false
                        Log.d(TAG, "onTouchEvent: mIsTouch = false")
                    }

                    else -> {
                        mIsTouch = false
                        Log.d(TAG, "onTouchEvent: mIsTouch = false")
                    }
                }
            }
        } else {
            mIsTouch = false
            Log.d(TAG, "onTouchEvent: mIsTouch = false")
        }
        invalidate()
        return true
    }

    private fun isRing(x: Float, y: Float): Boolean {
        val distance = sqrt((x - X).pow(2) + (y - Y).pow(2))
        return distance in innerRadius..outerRadius
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d(TAG, "onDraw: mTouchDownPosition = $mIsTouch")
        val gradient:RadialGradient = if (mIsTouch) {
            RadialGradient(
                rectF!!.centerX(),
                rectF!!.centerY(),
                outerRadius,
                mGradualStartColor,
                mGradualEndColor,
                Shader.TileMode.CLAMP
            )
        } else {
            RadialGradient(
                rectF!!.centerX(),
                rectF!!.centerY(),
                outerRadius,
                mGradualDefaultColor,
                mGradualDefaultColor,
                Shader.TileMode.CLAMP
            )
        }
        piePaint!!.shader = gradient
        canvas.drawArc(rectF!!, START_DEGREE + mPosition * 90, 90f, true, piePaint!!) //扇形
        drawTriangleView(canvas, rectF!!, trianglePaint!!, mPosition)
        canvas.drawArc(rectF!!, START_DEGREE + mPosition * 90, 90f, true, circlePaint!!) //扇形
    }

    private fun drawTriangleView(canvas: Canvas, rectF: RectF, paint: Paint, position: Int) {
        val path = Path()
        var x = rectF.centerX()
        var y = rectF.centerY()
        when (position) {
            0 -> {
                x = rectF.centerX() + triangleRadius
                y = rectF.centerY()
                path.moveTo(x, y)
                path.lineTo(x, y - triangleSide/2)
                path.lineTo(x + triangleHeight, y)
                path.lineTo(x, y + triangleSide/2)
            }
            1 -> {
                x = rectF.centerX()
                y = rectF.centerY() + triangleRadius
                path.moveTo(x, y)
                path.lineTo(x + triangleSide/2, y)
                path.lineTo(x, y + triangleHeight)
                path.lineTo(x - triangleSide/2, y)
            }
            2 -> {
                x = rectF.centerX() - triangleRadius
                y = rectF.centerY()
                path.moveTo(x, y)
                path.lineTo(x, y + triangleSide/2)
                path.lineTo(x - triangleHeight, y)
                path.lineTo(x, y - triangleSide/2)
            }
            3 -> {
                x = rectF.centerX()
                y = rectF.centerY() - triangleRadius
                path.moveTo(x, y)
                path.lineTo(x - triangleSide/2, y)
                path.lineTo(x, y - triangleHeight)
                path.lineTo(x + triangleSide/2, y)
            }
            else -> {
                path.moveTo(x, y)
            }
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    /**
     * dip转为 px
     */
    private fun dip2px(dipValue: Float): Int {
        val scale = context!!.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }
}

data class PieChartBean(
    var valuer: String? = null,      //说明
    var angle: Float = 0f,    //占的大小
    var shader: Shader? = null
)