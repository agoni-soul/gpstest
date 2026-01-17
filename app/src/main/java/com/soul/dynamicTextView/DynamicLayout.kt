package com.soul.dynamicTextView

import android.content.Context
import android.graphics.Color
import android.text.TextUtils.TruncateAt
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.soul.gpstest.R
import com.soul.util.DpOrSpToPxTransfer


/**
 * @auther: haha
 * @Date:   2026/1/16
 * @Detail:
 */
class DynamicLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ViewGroup(context, attrs, defStyleAttr) {

    private var horizontalSpacing: Int = DpOrSpToPxTransfer.dp2px(context, 20)

    private var mIconIv: ImageView
    private var mIconWidth: Int = DpOrSpToPxTransfer.dp2px(context, 40)
    private var mIconHeight: Int = DpOrSpToPxTransfer.dp2px(context, 40)
    private var mIconDrawableRes: Int = 0

    private var mContentTv: TextView
    private var mContentWidth: Int = LayoutParams.WRAP_CONTENT
    private var mContentHeight: Int = LayoutParams.WRAP_CONTENT
    private var mContentText: String = ""
    private var mContentColor: Int = Color.BLACK
    private var mContentSize: Int = DpOrSpToPxTransfer.sp2px(context, 40)
    private var mContentMaxLine: Int = 3

    private var mUnitTV: TextView
    private var mUnitWidth: Int = LayoutParams.WRAP_CONTENT
    private var mUnitHeight: Int = LayoutParams.WRAP_CONTENT
    private var mUnitText: String = ""
    private var mUnitColor: Int = Color.GRAY
    private var mUnitSize: Int = DpOrSpToPxTransfer.sp2px(context, 22)

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        removeAllViews()
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.dynamicLayout, defStyleAttr, 0)
        try {
            mIconWidth = typedArray.getLayoutDimension(
                R.styleable.dynamicLayout_iconWidth,
                DpOrSpToPxTransfer.dp2px(context, 20)
            )
            mIconHeight = typedArray.getLayoutDimension(
                R.styleable.dynamicLayout_iconHeight,
                DpOrSpToPxTransfer.dp2px(context, 20)
            )
            mIconDrawableRes = typedArray.getResourceId(R.styleable.dynamicLayout_iconUrl, 0)

            mContentWidth = typedArray.getLayoutDimension(
                R.styleable.dynamicLayout_contentWidth,
                LayoutParams.WRAP_CONTENT
            )
            mContentHeight = typedArray.getLayoutDimension(
                R.styleable.dynamicLayout_contentHeight,
                LayoutParams.WRAP_CONTENT
            )
            mContentText = typedArray.getString(R.styleable.dynamicLayout_contentText).toString()
            mContentSize = typedArray.getLayoutDimension(
                R.styleable.dynamicLayout_contentSize,
                DpOrSpToPxTransfer.sp2px(context, 40)
            )
            mContentColor = typedArray.getColor(
                R.styleable.dynamicLayout_contentColor,
                Color.BLACK
            )
            mContentMaxLine = typedArray.getInteger(R.styleable.dynamicLayout_contentMaxLine, 3)

            mUnitWidth = typedArray.getLayoutDimension(
                R.styleable.dynamicLayout_unitWidth,
                LayoutParams.WRAP_CONTENT
            )
            mUnitHeight = typedArray.getLayoutDimension(
                R.styleable.dynamicLayout_unitHeight,
                LayoutParams.WRAP_CONTENT
            )
            mUnitText = typedArray.getString(R.styleable.dynamicLayout_unitText).toString()
            mUnitSize = typedArray.getLayoutDimension(
                R.styleable.dynamicLayout_unitSize,
                DpOrSpToPxTransfer.sp2px(context, 14)
            )
            mUnitColor = typedArray.getColor(
                R.styleable.dynamicLayout_unitColor,
                Color.BLACK
            )
        } catch (e: Exception) {
            e.printStackTrace()
            println("e: ${e.message}")
        } finally {
            typedArray.recycle()
        }

        mIconIv = ImageView(context)
        var params = MarginLayoutParams(mIconWidth, mIconHeight)
        mIconIv.layoutParams = params
        if (mIconDrawableRes != 0) {
            mIconIv.setImageResource(mIconDrawableRes)
        }
        addView(mIconIv)

        mContentTv = TextView(context)
        params = MarginLayoutParams(mContentWidth, mContentHeight)
        mContentTv.layoutParams = params
        mContentTv.text = mContentText.ifEmpty { "哈" }
        mContentTv.includeFontPadding = false
        mContentTv.textSize = mContentSize.toFloat()
        mContentTv.setTextColor(mContentColor)
        mContentTv.maxLines = mContentMaxLine
        val paint = mContentTv.paint
        val fontMetrics = paint.fontMetrics
        // 计算底部空白并调整
        val bottomSpace = fontMetrics.bottom// - fontMetrics.descent
        mContentTv.setPadding(0, 0, 0, -bottomSpace.toInt())
        mContentTv.ellipsize = TruncateAt.END
        addView(mContentTv)

        mUnitTV = TextView(context)
        params = MarginLayoutParams(mUnitWidth, mUnitHeight)
        mUnitTV.layoutParams = params
        mUnitTV.text = mUnitText.ifEmpty { "立方米" }
        mUnitTV.textSize = mUnitSize.toFloat()
        mUnitTV.setTextColor(mUnitColor)
        addView(mUnitTV)
    }

    fun setFontPaddingTop(isFontPaddingTop: Boolean) {
        if (mContentTv is FontPaddingTextView) {
            (mContentTv as FontPaddingTextView).setFontPaddingTop(isFontPaddingTop)
        }
    }

    fun setFontPaddingBottom(isFontPaddingBottom: Boolean) {
        if (mContentTv is FontPaddingTextView) {
            (mContentTv as FontPaddingTextView).setFontPaddingBottom(isFontPaddingBottom)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        println("onMeasure")
        maxHeight = 0
        // 获取父容器给的限制
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        // 实际测量时的可用宽度（减去padding）
        val availableWidth: Int = widthSize - paddingLeft - paddingRight
        var leavingWidth = availableWidth
        leavingWidth =
            calculateChildMeasure(widthMeasureSpec, heightMeasureSpec, leavingWidth, mIconIv)
        if (leavingWidth < 0) {
            throw Exception("no enough space.")
        }
        leavingWidth =
            calculateChildMeasure(widthMeasureSpec, heightMeasureSpec, leavingWidth, mUnitTV)
        if (leavingWidth < 0) {
            throw Exception("no enough space.")
        }

        measureChildWithMargins(
            mContentTv, widthMeasureSpec, 0,
            heightMeasureSpec, 0
        )
        // 获取子View的测量尺寸（包括margin）
        var lp = mContentTv.layoutParams as MarginLayoutParams
        var childWidth = mContentTv.measuredWidth + lp.leftMargin + lp.rightMargin
        var childHeight = mContentTv.measuredHeight + lp.topMargin + lp.bottomMargin
        if (childWidth > leavingWidth) {
            // 使用固定宽度
            val widthSpec = MeasureSpec.makeMeasureSpec(
                leavingWidth,
                MeasureSpec.AT_MOST
            )
            measureChildWithMargins(
                mContentTv, widthSpec, 0,
                heightMeasureSpec, 0
            )
            // 获取子View的测量尺寸（包括margin）
            lp = mContentTv.layoutParams as MarginLayoutParams
            childWidth = mContentTv.measuredWidth + lp.leftMargin + lp.rightMargin
            childHeight = mContentTv.measuredHeight + lp.topMargin + lp.bottomMargin
        }
        maxHeight = maxHeight.coerceAtLeast(childHeight)
        leavingWidth -= childWidth

        // 根据父容器的测量模式调整最终尺寸
        val measuredWidth = resolveSize(availableWidth - leavingWidth, widthMeasureSpec)
        val measuredHeight = resolveSize(maxHeight, heightMeasureSpec)

        // 设置最终测量结果
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    private var maxWidth: Int = 0
    private var maxHeight: Int = 0

    private fun calculateChildMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        leavingWidth: Int,
        child: View?
    ): Int {
        if (child == null || child.visibility == GONE) return leavingWidth
        measureChildWithMargins(
            child, widthMeasureSpec, 0,
            heightMeasureSpec, 0
        )
        // 获取子View的测量尺寸（包括margin）
        val lp = child.layoutParams as MarginLayoutParams
        val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
        val childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin
        maxHeight = maxHeight.coerceAtLeast(childHeight)
        val leaving = leavingWidth - childWidth - horizontalSpacing
        return leaving
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        println("onLayout: ")
        mStartTop = 0
        mStartLeft = 0
        if (!changed) {
            return
        }
        val paddingT = paddingTop
        val paddingL = paddingLeft
        val paddingR = paddingRight
        val paddingB = paddingBottom
        println("l = $l, t = $t, r = $r, b = $b, paddingL = $paddingT, paddingR = $paddingR, measuredWidth = $measuredWidth, width = $width")
        mStartLeft = paddingL + l
        mStartTop = paddingT + t

        val count = childCount
        var sum = 0
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue
            val lp = child.layoutParams as MarginLayoutParams
            sum += (lp.leftMargin + lp.rightMargin + child.measuredWidth)
        }
        mStartLeft += (width - paddingL - paddingR - sum) / 2

        calculateChildLayout(mIconIv)
        calculateChildLayout(mContentTv)
        calculateChildLayout(mUnitTV)
    }

    private var mStartLeft = 0
    private var mStartTop = 0

    private fun calculateChildLayout(child: View?) {
        if (child == null || child.visibility == GONE) return
        val childWidth = child.measuredWidth
        val childHeight = child.measuredHeight

        val childLp = child.layoutParams as MarginLayoutParams
        val childLeft = mStartLeft + childLp.leftMargin

        child.layout(
            childLeft,
            maxHeight - childHeight - childLp.topMargin,
            childLeft + childWidth,
            maxHeight
        )
        mStartLeft += childWidth + horizontalSpacing
    }
}