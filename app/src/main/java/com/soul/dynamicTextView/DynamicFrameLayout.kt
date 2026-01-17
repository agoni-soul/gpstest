package com.soul.dynamicTextView

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

/**
 * @auther: haha
 * @Date:   2026/1/17
 * @Detail:
 */
class DynamicFrameLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ViewGroup(context, attrs, defStyleAttr) {

    private var mTotalWidth = 0
    private var mTotalHeight = 0

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    // 如果要支持MarginLayoutParams
    override fun generateDefaultLayoutParams(): LayoutParams {
        // Return MarginLayoutParams instead of LayoutParams
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun checkLayoutParams(p: LayoutParams?): Boolean {
        return p is MarginLayoutParams
    }

//    override fun getLayoutDirection(): Int {
//        return LayoutDirection.LTR
//    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        println("onMeasure")
        mTotalWidth = 0
        mTotalHeight = 0
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val availableWidth = widthSize - paddingLeft - paddingRight
        val availableHeight = heightSize - paddingTop - paddingBottom

        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue
            measureChildWithMargins(
                child, widthMeasureSpec, 0,
                heightMeasureSpec, 0
            )
            var lp = child.layoutParams as MarginLayoutParams
            var childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
            var childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin
            if (mTotalWidth + childWidth > availableWidth) {
                val leaving = availableWidth - mTotalWidth
                // 长度不够时，用剩下长度使用[MeasureSpec.AT_MOST]设置
                val childSpec = MeasureSpec.makeMeasureSpec(leaving, MeasureSpec.AT_MOST)
                measureChildWithMargins(
                    child, childSpec, 0,
                    heightMeasureSpec, 0
                )
                lp = child.layoutParams as MarginLayoutParams
                childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
                childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin
            }
            mTotalHeight = mTotalHeight.coerceAtLeast(childHeight)
            mTotalWidth += childWidth
        }
        val width = resolveSize(mTotalWidth, widthMeasureSpec)
        val height = resolveSize(mTotalHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private var mStartLeft = 0
    private var mStartTop = 0

    override fun onLayout(
        changed: Boolean,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) {
        println("onLayout: changed = ${changed}, l = $l, t = $t, r = $r, b = $b")
        val totalWidth = r - l
        val totalHeight = b - t
        mStartLeft = paddingLeft
        mStartTop = (totalHeight - paddingTop - paddingBottom) / 2
        println("totalWidth = $totalWidth, totalHeight = $totalHeight")
        if (!changed) return

        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue
            val lp = child.layoutParams as MarginLayoutParams
            val childWidth = child.measuredWidth //+ lp.leftMargin + lp.rightMargin
            val childHeight = child.measuredHeight// + lp.topMargin + lp.bottomMargin
            val childHeightHalf = childHeight / 2
            // 先加上左边的间距leftMargin
            mStartLeft += lp.leftMargin
            child.layout(
                mStartLeft, mStartTop - childHeightHalf,
                mStartLeft + childWidth, mStartTop + childHeightHalf
            )
            // 再加上View内容长度，以及右边间距rightMargin
            mStartLeft += (childWidth + lp.rightMargin)
        }
    }
}