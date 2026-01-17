package com.soul.dynamicTextView

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView
import com.soul.gpstest.R

/**
 * @auther: haha
 * @Date:   2026/1/17
 * @Detail:
 */
class FontPaddingTextView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    AppCompatTextView(context, attrs, defStyleAttr) {

    private val TAG = javaClass.simpleName

    private var mIsFontPaddingTop = true
    private var mIsFontPaddingBottom = true

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.fontPaddingTextView, defStyleAttr, 0)
        try {
            mIsFontPaddingTop =
                typedArray.getBoolean(R.styleable.fontPaddingTextView_fontPaddingTop, true)
            mIsFontPaddingBottom =
                typedArray.getBoolean(R.styleable.fontPaddingTextView_fontPaddingBottom, true)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.printStackTrace().toString())
        } finally {
            typedArray.recycle()
        }
        includeFontPadding = false
    }

    fun setFontPaddingTop(isFontPaddingTop: Boolean) {
        mIsFontPaddingTop = isFontPaddingTop
    }

    fun setFontPaddingBottom(isFontPaddingBottom: Boolean) {
        mIsFontPaddingBottom = isFontPaddingBottom
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val paint = paint
        val fontMetrics = paint.fontMetrics
        var sum = measuredHeight
        println("top = ${fontMetrics.top}, bottom = ${fontMetrics.bottom}")
        if (!mIsFontPaddingTop) {
            sum += fontMetrics.top.toInt()
        }
        if (!mIsFontPaddingBottom) {
            sum -= fontMetrics.bottom.toInt()
        }
        setMeasuredDimension(measuredWidth, sum)
    }

    override fun onDraw(canvas: Canvas) {
        // 获取字体度量
//        val paint = paint
//        val fontMetrics = paint.fontMetrics
//
//        // 计算文字实际高度（不含空白）
////        val textHeight = -fontMetrics.ascent - fontMetrics.descent
//        val textHeight = 0.toFloat()
//
//        // 调整绘制位置，去掉顶部空白
//        var baseline = (height - textHeight) / 2
//        if (!mIsFontPaddingTop) {
//            baseline += fontMetrics.top
//        }
//        if (!mIsFontPaddingBottom) {
////            baseline -= fontMetrics.bottom
//        }
//        canvas.translate(0f, baseline)
        super.onDraw(canvas)
    }
}