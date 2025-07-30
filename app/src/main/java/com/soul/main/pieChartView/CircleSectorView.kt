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
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
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
class CircleSectorView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
        private val TAG = this.javaClass.simpleName
    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private var piePaint: Paint? = null //扇形画笔
    private var outerLinePaint: Paint? = null //轮廓画笔
    private var linePaint: Paint? = null //指示线
    private var textPaint: Paint? = null //文字画笔
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
    private var mSweep = mutableListOf<Float>()

    private var mGradualDefaultColor: Int = Color.parseColor("#F9F9F9")
    private var mGradualStartColor: Int = Color.parseColor("#FDFDFD")
    private var mGradualEndColor: Int = Color.parseColor("#E5E5E8")
    private var mTriangleBgColor: Int = Color.parseColor("#C7C7CC")
    private var mInnerBgColor: Int = Color.parseColor("#F0F0F0")
    private var mTouchDownPosition = -1
    private var mTouchDownPaint: Paint? = null

    private var context: Context? = context
    private var list: MutableList<PieChartBean>? = null
    private var mCallback: ((Int, Boolean) -> Unit)? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        outerRadius = (min(measuredWidth, measuredHeight) / 2.0).toFloat()

        X = (measuredWidth / 2.0).toFloat()
        Y = (measuredHeight / 2.0).toFloat()
        rectF!!.left = X - outerRadius
        rectF!!.top = Y - outerRadius
        rectF!!.right = X + outerRadius
        rectF!!.bottom = Y + outerRadius
    }

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

        circlePaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            alpha = 255
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
        linePaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = 4f
        }

        textPaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = 30f
            textSize = 25f
        }
    }

    fun setDate(list: MutableList<PieChartBean>) {
        this.list?.clear()
        if (this.list == null) {
            this.list = mutableListOf()
        }
        this.list?.addAll(list)
        mSweep.clear()
        list.apply {
            for (i in indices) {
                mSweep.add(90f)
            }
        }
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            Log.d(TAG, "onTouchEvent: event = null")
            return super.onTouchEvent(event)
        }
        val action = event.action
        val x = event.x
        val y = event.y
        var position = -1
        if (isRing(x, y)) {
            val centerX = rectF!!.centerX()
            val centerY = rectF!!.centerY()
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
            Log.d(TAG, "onTouchEvent: position = $position, action = ${action}")
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    mCallback?.invoke(position, true)
                    mTouchDownPosition = position
                    Log.d(TAG, "onTouchEvent: touchDownPosition = $mTouchDownPosition")
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
//                    mCallback?.invoke(position, true)
                    mTouchDownPosition = position
                    Log.d(TAG, "onTouchEvent: touchDownPosition = $mTouchDownPosition")
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mCallback?.invoke(position, false)
                    mTouchDownPosition = -1
                    Log.d(TAG, "onTouchEvent: touchDownPosition = $mTouchDownPosition")
                }

                else -> {
                    mTouchDownPosition = -1
                    Log.d(TAG, "onTouchEvent: touchDownPosition = $mTouchDownPosition")
                }
            }
        } else {
            mTouchDownPosition = -1
            Log.d(TAG, "onTouchEvent: touchDownPosition = $mTouchDownPosition")
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
        Log.d(TAG, "onDraw: mTouchDownPosition = $mTouchDownPosition")
        initPie(canvas)
    }

    /**
     * 画扇形
     * @param canvas
     */
    private fun initPie(canvas: Canvas) {
        if (rectF == null || list.isNullOrEmpty()) return
        val anglesSize = list!!.size
        if (anglesSize > 0 && mSweep.size == anglesSize) {
            var pieStart = START_DEGREE
            for (i in 0 until anglesSize) {
                if (mTouchDownPosition >= 0) {
                    val gradient:RadialGradient = RadialGradient(
                        rectF!!.centerX(),
                        rectF!!.centerY(),
                        outerRadius,
                        mGradualStartColor,
                        mGradualEndColor,
                        Shader.TileMode.CLAMP
                    )
                    piePaint!!.color = Color.BLUE
                    canvas.drawArc(rectF!!, pieStart, mSweep[i], true, piePaint!!) //扇形
                } else {
                    val gradient:RadialGradient = RadialGradient(
                        rectF!!.centerX(),
                        rectF!!.centerY(),
                        outerRadius,
                        mGradualDefaultColor,
                        mGradualDefaultColor,
                        Shader.TileMode.CLAMP
                    )
                    mTouchDownPaint!!.color = Color.RED
                    canvas.drawArc(rectF!!, pieStart, mSweep[i], true, mTouchDownPaint!!) //扇形
                }
                drawTriangleView(canvas, rectF!!, trianglePaint!!, i)
//                canvas.drawArc(rectF!!, pieStart, mSweep[i], true, outerLinePaint!!) //边框线
//                initLineAndText(
//                    canvas, pieStart, mSweep!![i] ?: 0f, Color.BLUE, list!![i].valuer ?: ""
//                )
                pieStart += mSweep[i]
            }
            canvas.drawCircle(rectF!!.centerX(), rectF!!.centerY(), innerRadius, circlePaint!!)
        }
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
     * 画指示线 和文字
     * @param canvas
     * @param text
     */
    private fun initLineAndText(
        canvas: Canvas, statrAngles: Float, angles: Float, color: Int, text: String
    ) {
        val stopX: Float
        val stopY: Float
        val centerX = (measuredWidth / 2).toFloat()
        val centerY = (measuredHeight / 2).toFloat()
        linePaint!!.color = resources.getColor(R.color.black)
        textPaint!!.color = resources.getColor(R.color.blue)
        //  半径加上多出的20个像素的位置，去根据角度算出 转点的X,Y轴
        val cosX = cos((2 * statrAngles + angles) / 2 * Math.PI / 180).toFloat()
        val sinY = sin((2 * statrAngles + angles) / 2 * Math.PI / 180).toFloat()
        stopX = (outerRadius + dip2px(10f)) * cosX
        stopY = (outerRadius + dip2px(10f)) * sinY
        //扇形弧边的中点的X,Y 为起点，然后算出中间点的角度，加上半径+10个像素 得出终点的XY轴，
        canvas.drawLine(
            centerX + (outerRadius - dip2px(10f)) * cosX,
            centerY + (outerRadius - dip2px(10f)) * sinY,
            stopX + centerX,
            stopY + centerY,
            linePaint!!
        )
        val rect = Rect()
        textPaint!!.getTextBounds(text, 0, text.length, rect)
        val h = rect.height()
        val w = rect.width()
        //画第二根线，第二个根线的起点是第一根线的终点，然后终点，根据X轴来定直接加25个像素
        //如果是右边，减去25个像素
        //文字的位置，Y轴根据第二根线的Y轴+文字的高度的一半，这样就能居中  X轴，左边加30个像素，根第二个线的有5个像素的距离
        //文字如果在右边，减去文字的宽度 - 30个像素
        if (stopX > 0) {
            //50为横线的长度 60 为文字的偏移量
            canvas.drawLine(
                centerX + stopX,
                centerY + stopY,
                centerX + stopX + dip2px(25f),
                centerY + stopY,
                linePaint!!
            )
            canvas.drawText(
                text,
                0,
                text.length,
                centerX + stopX + dip2px(30f),
                centerY + stopY + h / 2,
                textPaint!!
            )
        } else {
            canvas.drawLine(
                centerX + stopX,
                centerY + stopY,
                centerX + stopX - dip2px(25f),
                centerY + stopY,
                linePaint!!
            )
            canvas.drawText(
                text,
                0,
                text.length,
                centerX + stopX - w - dip2px(30f),
                centerY + stopY + h / 2,
                textPaint!!
            )
        }
    }

    /**
     * dip转为 px
     */
    private fun dip2px(dipValue: Float): Int {
        val scale = context!!.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }


    private val PIE_ANIMATION_VALUE = 100f

    private inner class PieChartAnimation : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            super.applyTransformation(interpolatedTime, t)
            list?.apply {
                mSweep.clear()
                if (interpolatedTime < 1.0f) {
                    for (i in indices) {
                        //根据传进来的比例，算出在圆中所占的角度
                        mSweep.add(get(i).angle * interpolatedTime / PIE_ANIMATION_VALUE * 360)
                    }
                } else {
                    for (i in indices) {
                        mSweep.add(get(i).angle / PIE_ANIMATION_VALUE * 360)
                    }
                }
            }
            invalidate()
        }
    }
}

//data class PieChartBean(
//    var valuer: String? = null,      //说明
//    var angle: Float = 0f,    //占的大小
//    var shader: Shader? = null
//)