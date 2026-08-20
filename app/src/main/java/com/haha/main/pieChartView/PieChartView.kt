package com.haha.main.pieChartView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.haha.hahalearn.R
import com.haha.util.DpOrSpToPxTransfer
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt


/**
 *
 * @author : haha
 * @date   : 2024-11-23
 * @desc   : 扇形圆
 *
 */
class PieChartView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
    private val TAG = this.javaClass.simpleName

    private val colors = Array(4) { IntArray(2) { 0 } }
    private val mStartAngle = -45f // 起始角度

    private var mPiePaint: Paint? = null //扇形画笔
    private var mCirclePaint: Paint? = null
    private var mTrianglePaint: Paint? = null

    private var mOuterRadius = 0f
    private var mInnerRadius = 0f
    private var mTriangleRadius = 0f
    private var mTriangleSide = 0f
    private var mTriangleHeight = 0f

    //饼状图动画效果
    private var X = 0f
    private var Y = 0f
    private var mRectF: RectF? = null

    private var mGradualDefaultColor: Int = 0
    private var mGradualStartColor: Int = 0
    private var mGradualEndColor: Int = 0
    private var mTriangleColor: Int = 0
    private var mInnerCircleColor: Int = 0

    private var mTouchCallback: ((Int, Boolean) -> Unit)? = null
    private var mLastTouchPosition = -1

    private var mGradientShader: RadialGradient? = null
    private var mConstantShader: RadialGradient? = null

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.circleSectorView, defStyleAttr, 0)
        mOuterRadius = typedArray.getDimensionPixelSize(
            R.styleable.circleSectorView_outerRadius,
            DpOrSpToPxTransfer.dp2px(context, 108)
        ).toFloat()
        mGradualDefaultColor = typedArray.getColor(
            R.styleable.circleSectorView_sectorBackgroundColor,
            Color.parseColor("#F9F9F9")
        )
        mGradualStartColor = typedArray.getColor(
            R.styleable.circleSectorView_sectorGradualStartColor,
            Color.parseColor("#FDFDFD")
        )
        mGradualEndColor = typedArray.getColor(
            R.styleable.circleSectorView_sectorGradualEndColor,
            Color.parseColor("#E5E5E8")
        )
        mInnerRadius = typedArray.getDimensionPixelSize(
            R.styleable.circleSectorView_innerRadius,
            DpOrSpToPxTransfer.dp2px(context, 24)
        ).toFloat()
        mInnerCircleColor = typedArray.getColor(
            R.styleable.circleSectorView_innerCircleColor,
            Color.parseColor("#F0F0F0")
        )
        mTriangleRadius = typedArray.getDimensionPixelSize(
            R.styleable.circleSectorView_triangleRadius,
            DpOrSpToPxTransfer.dp2px(context, 78)
        ).toFloat()
        mTriangleColor = typedArray.getColor(
            R.styleable.circleSectorView_triangleColor,
            Color.parseColor("#C7C7CC")
        )
        mTriangleSide = typedArray.getDimensionPixelSize(
            R.styleable.circleSectorView_triangleSide,
            DpOrSpToPxTransfer.dp2px(context, 14)
        ).toFloat()
        mTriangleHeight = typedArray.getDimensionPixelSize(
            R.styleable.circleSectorView_triangleHeight,
            DpOrSpToPxTransfer.dp2px(context, 6)
        ).toFloat()

        colors.forEach {
            it[0] = mGradualDefaultColor
            it[1] = mGradualDefaultColor
        }

        mPiePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        mCirclePaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            alpha = 255
            color = mInnerCircleColor
        }

        mTrianglePaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            alpha = 255
            color = mTriangleColor
        }

        mRectF = RectF()
    }

    fun setTouchCallback(touchCallback: (Int, Boolean) -> Unit) {
        mTouchCallback = touchCallback
    }

    fun setOuterRadius(outRadius: Float) {
        mOuterRadius = outRadius
    }

    fun setSectorBgColor(color: Int) {
        mGradualDefaultColor = color
        mConstantShader = RadialGradient(
            X,
            Y,
            mOuterRadius,
            mGradualDefaultColor,
            mGradualDefaultColor,
            Shader.TileMode.CLAMP
        )
    }

    fun setGradualStartAndEndColor(startColor: Int = Int.MIN_VALUE, endColor: Int = Int.MIN_VALUE) {
        if (startColor != Int.MIN_VALUE) {
            mGradualStartColor = startColor
        }
        if (endColor != Int.MIN_VALUE) {
            mGradualEndColor = endColor
        }
        mGradientShader = RadialGradient(
            X,
            Y,
            mOuterRadius,
            mGradualStartColor,
            mGradualEndColor,
            Shader.TileMode.CLAMP
        )
    }

    fun setInnerCircleColor(innerColor: Int) {
        mInnerCircleColor = innerColor
    }

    fun setInnerRadius(innerRadius: Float) {
        mInnerRadius = innerRadius
    }

    fun setTriangleColor(triangleColor: Int) {
        mTriangleColor = triangleColor
    }

    fun setTriangleRadius(triangleRadius: Float) {
        mTriangleRadius = triangleRadius
    }

    fun setTriangleSide(triangleSide: Float) {
        mTriangleSide = triangleSide
    }

    fun setTriangleHeight(triangleHeight: Float) {
        mTriangleHeight = triangleHeight
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mOuterRadius <= 0) {
            mOuterRadius = (min(measuredWidth, measuredHeight) / 2.0).toFloat()
        }

        X = (measuredWidth / 2.0).toFloat()
        Y = (measuredHeight / 2.0).toFloat()
        mRectF!!.left = X - mOuterRadius
        mRectF!!.top = Y - mOuterRadius
        mRectF!!.right = X + mOuterRadius
        mRectF!!.bottom = Y + mOuterRadius

        if (mGradientShader == null) {
            mGradientShader = RadialGradient(
                X,
                Y,
                mOuterRadius,
                mGradualStartColor,
                mGradualEndColor,
                Shader.TileMode.CLAMP
            )
        }
        if (mConstantShader == null) {
            mConstantShader = RadialGradient(
                X,
                Y,
                mOuterRadius,
                mGradualDefaultColor,
                mGradualDefaultColor,
                Shader.TileMode.CLAMP
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in colors.indices) {
            mPiePaint!!.shader = if (i == mLastTouchPosition) mGradientShader else mConstantShader
            canvas.drawArc(mRectF!!, mStartAngle + i * 90, 90f, true, mPiePaint!!)
            drawTriangleView(canvas, mRectF!!, mTrianglePaint!!, i)
        }
        canvas.drawCircle(mRectF!!.centerX(), mRectF!!.centerY(), mInnerRadius, mCirclePaint!!)
    }

    private fun drawTriangleView(canvas: Canvas, rectF: RectF, paint: Paint, position: Int) {
        val path = Path()
        var x = rectF.centerX()
        var y = rectF.centerY()
        when (position) {
            0 -> {
                x = rectF.centerX() + mTriangleRadius
                y = rectF.centerY()
                path.moveTo(x, y)
                path.lineTo(x, y - mTriangleSide / 2)
                path.lineTo(x + mTriangleHeight, y)
                path.lineTo(x, y + mTriangleSide / 2)
            }

            1 -> {
                x = rectF.centerX()
                y = rectF.centerY() + mTriangleRadius
                path.moveTo(x, y)
                path.lineTo(x + mTriangleSide / 2, y)
                path.lineTo(x, y + mTriangleHeight)
                path.lineTo(x - mTriangleSide / 2, y)
            }

            2 -> {
                x = rectF.centerX() - mTriangleRadius
                y = rectF.centerY()
                path.moveTo(x, y)
                path.lineTo(x, y + mTriangleSide / 2)
                path.lineTo(x - mTriangleHeight, y)
                path.lineTo(x, y - mTriangleSide / 2)
            }

            3 -> {
                x = rectF.centerX()
                y = rectF.centerY() - mTriangleRadius
                path.moveTo(x, y)
                path.lineTo(x - mTriangleSide / 2, y)
                path.lineTo(x, y - mTriangleHeight)
                path.lineTo(x + mTriangleSide / 2, y)
            }

            else -> {
                path.moveTo(x, y)
            }
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        val x = event.x
        val y = event.y
        var position: Int = -1
        if (isRing(x, y)) {
            val centerX = mRectF!!.centerX()
            val centerY = mRectF!!.centerY()
            val angle = Math.toDegrees(atan2((y - centerY).toDouble(), (x - centerX).toDouble()))
            Log.d(
                TAG,
                "angle = $angle, d = ${atan2((y - centerY).toDouble(), (x - centerX).toDouble())}"
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
            Log.d(TAG, "onTouchEvent: position = $position, action = $action")
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    mTouchCallback?.invoke(position, true)
                    if (mLastTouchPosition != position) {
                        if (mLastTouchPosition > -1) {
                            colors[mLastTouchPosition][0] = mGradualDefaultColor
                            colors[mLastTouchPosition][1] = mGradualDefaultColor
                        }
                        colors[position][0] = mGradualStartColor
                        colors[position][1] = mGradualEndColor
                        mLastTouchPosition = position
                    }
                    Log.d(
                        TAG,
                        "onTouchEvent: lastTouchPosition = $mLastTouchPosition, position = $position"
                    )
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (mLastTouchPosition != position) {
                        mTouchCallback?.invoke(mLastTouchPosition, false)
                        mTouchCallback?.invoke(position, true)
                        if (mLastTouchPosition > -1) {
                            colors[mLastTouchPosition][0] = mGradualDefaultColor
                            colors[mLastTouchPosition][1] = mGradualDefaultColor
                        }
                        colors[position][0] = mGradualStartColor
                        colors[position][1] = mGradualEndColor
                        mLastTouchPosition = position
                    }
                    Log.d(
                        TAG,
                        "onTouchEvent: lastTouchPosition = $mLastTouchPosition, position = $position"
                    )
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mTouchCallback?.invoke(position, false)
                    if (mLastTouchPosition != position) {
                        if (mLastTouchPosition > -1) {
                            colors[mLastTouchPosition][0] = mGradualDefaultColor
                            colors[mLastTouchPosition][1] = mGradualDefaultColor
                        }
                    }
                    colors[position][0] = mGradualDefaultColor
                    colors[position][1] = mGradualDefaultColor
                    mLastTouchPosition = -1
                    Log.d(
                        TAG,
                        "onTouchEvent: lastTouchPosition = $mLastTouchPosition, position = $position"
                    )
                }

                else -> {
                    if (mLastTouchPosition != position) {
                        if (mLastTouchPosition > -1) {
                            colors[mLastTouchPosition][0] = mGradualDefaultColor
                            colors[mLastTouchPosition][1] = mGradualDefaultColor
                        }
                    }
                    colors[position][0] = mGradualDefaultColor
                    colors[position][1] = mGradualDefaultColor
                    mLastTouchPosition = -1
                    Log.d(
                        TAG,
                        "onTouchEvent: lastTouchPosition = $mLastTouchPosition, position = $position"
                    )
                }
            }
        } else {
            if (mLastTouchPosition > -1) {
                colors[mLastTouchPosition][0] = mGradualDefaultColor
                colors[mLastTouchPosition][1] = mGradualDefaultColor
            }
            mLastTouchPosition = -1
            Log.d(TAG, "onTouchEvent: lastTouchPosition = $mLastTouchPosition")
        }
        invalidate()
        return true
    }

    private fun isRing(x: Float, y: Float): Boolean {
        val distance = sqrt((x - X).pow(2) + (y - Y).pow(2))
        return distance in mInnerRadius..mOuterRadius
    }
}