package com.soul.main

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.soul.gpstest.R
import com.soul.util.DpOrSpToPxTransfer


/**
 *     author : yangzy33
 *     time   : 2024-07-16
 *     desc   :
 *     version: 1.0
 */
class CircleProgressView(context: Context, attrs: AttributeSet?, defStyleAttr: Int): View(context, attrs, defStyleAttr) {
    private val TAG = this.javaClass.simpleName
    private val mContext = context
    private var progressPaint: Paint
    private var backgroundPaint: Paint
    private var rectF: RectF
    private var progress = 0f
    private var mProgressColor: Int = Color.BLUE
    private var mCircleBgColor: Int = Color.LTGRAY

    private val mArcWidth: Float
    private val mCircleRadius: Int

    private val centerTextPaint: Paint
    private var mCenterTextColor: Int
    private var mCenterTextSize: Float
    private var textBoundRect: Rect
    private var mCenterText: String? = null

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        val typedArray =
            mContext.obtainStyledAttributes(attrs, R.styleable.circlePercentBar, defStyleAttr, 0)
        mArcWidth = typedArray.getDimensionPixelSize(R.styleable.circlePercentBar_arcWidth, DpOrSpToPxTransfer.dp2px(context, 4)).toFloat()
        mCircleRadius = typedArray.getDimensionPixelSize(R.styleable.circlePercentBar_circleRadius, DpOrSpToPxTransfer.dp2px(context, 4))
        mProgressColor = typedArray.getColor(
            R.styleable.circlePercentBar_arcProgressColor,
            Color.BLUE
        )
        mCircleBgColor = typedArray.getColor(
            R.styleable.circlePercentBar_arcCircleBgColor,
            Color.LTGRAY
        )

        mCenterTextColor =
            typedArray.getColor(R.styleable.circlePercentBar_centerTextColor, 0x0000ff)
        mCenterTextSize = typedArray.getDimensionPixelSize(R.styleable.circlePercentBar_centerTextSize, DpOrSpToPxTransfer.sp2px(context, 24)).toFloat()
        typedArray.recycle()

        progressPaint = Paint()
        progressPaint.color = mProgressColor
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeWidth = mArcWidth
        progressPaint.isAntiAlias = true

        backgroundPaint = Paint()
        backgroundPaint.color = mCircleBgColor
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeWidth = mArcWidth
        backgroundPaint.isAntiAlias = true
        rectF = RectF() // 初始化时设置大小和位置

        centerTextPaint = Paint()
        centerTextPaint.style = Paint.Style.FILL
        centerTextPaint.color = mCenterTextColor
        centerTextPaint.textSize = mCenterTextSize
        textBoundRect = Rect()
    }

    fun setProgress(progress: Float) {
        this.progress = progress
//        invalidate() // 重绘视图
    }

    fun setProgressColor(progressColor: Int) {
        mProgressColor = progressColor
    }

    fun setCircleBgColor(circleBgColor: Int) {
        mCircleBgColor = circleBgColor
    }

    fun setCenterTextSize(centerTextSize: Float) {
        mCenterTextSize = centerTextSize
    }

    fun setCenterTextColor(centerTextColor: Int) {
        mCenterTextColor = centerTextColor
    }

    fun setCenterText(centerText: String?) {
        mCenterText = centerText
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            measureDimension(widthMeasureSpec),
            measureDimension(heightMeasureSpec)
        )
    }

    private fun measureDimension(measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = mCircleRadius * 2
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        rectF.set(
            (width / 2 - mCircleRadius + mArcWidth / 2),
            (height / 2 - mCircleRadius + mArcWidth / 2),
            (width / 2 + mCircleRadius - mArcWidth / 2),
            (height / 2 + mCircleRadius - mArcWidth / 2)
        )
        // 绘制背景圆环
        canvas.drawOval(rectF, backgroundPaint)
        progressPaint.shader = SweepGradient(
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            mCircleBgColor,
            mProgressColor
        )
        // 绘制进度圆环
        canvas.drawArc(rectF, -90f, progress, false, progressPaint)

        textBoundRect.set(
            (mCircleRadius + mArcWidth).toInt(),
            (mCircleRadius + mArcWidth).toInt(),
            (mCircleRadius - mArcWidth).toInt(),
            (mCircleRadius - mArcWidth).toInt()
        )
        val data = mCenterText ?: "${progress.toInt()}%"
        var i = data.length + 1
        var text: String
        do {
            i --
            text = if (i == data.length) data else "${data.substring(0, i)}..."
            val measureWidth = centerTextPaint.measureText(text)
        } while (measureWidth > (mCircleRadius * 2) && i >= 0)
        centerTextPaint.getTextBounds(text, 0, text.length, textBoundRect)
        canvas.drawText(
            text,
            (width / 2f - textBoundRect.width() / 2f).coerceAtLeast(width/2f - mCircleRadius + mArcWidth),
            (height / 2f + textBoundRect.height() / 2f),
            centerTextPaint
        )
    }
}