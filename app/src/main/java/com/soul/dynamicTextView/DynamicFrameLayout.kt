package com.soul.dynamicTextView

import android.content.Context
import android.util.AttributeSet
import android.view.View
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
    private val mAllViewIndex = mutableListOf<Int>()
    private val mLineHeightList = mutableListOf<Int>()

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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        println("onMeasure")
        mAllViewIndex.clear()
        mLineHeightList.clear()
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

        var lineMaxHeight = 0
        var lineViewList = mutableListOf<View>()
        var lineTotalWidth = 0

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
            var isAdjust = false
            if (lineTotalWidth + childWidth > availableWidth) {
                val leaving = availableWidth - lineTotalWidth
                // 长度不够时，用剩下长度使用[MeasureSpec.AT_MOST]设置
                val childSpec = MeasureSpec.makeMeasureSpec(leaving, MeasureSpec.AT_MOST)
                measureChildWithMargins(
                    child, childSpec, 0,
                    heightMeasureSpec, 0
                )
                lp = child.layoutParams as MarginLayoutParams
                childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
                childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin
                isAdjust = true
            }
            if (lineTotalWidth + childWidth > availableWidth ||
                (!lineViewList.isEmpty() && childHeight > lineMaxHeight)
            ) {
                mAllViewIndex.add(i)
                mLineHeightList.add(lineMaxHeight)
                mTotalWidth = mTotalWidth.coerceAtLeast(lineTotalWidth)
                lineTotalWidth = 0
                lineViewList = mutableListOf()
                mTotalHeight += lineMaxHeight

                if (isAdjust) {
                    val leaving = availableWidth
                    val childSpec = MeasureSpec.makeMeasureSpec(leaving, MeasureSpec.AT_MOST)
                    measureChildWithMargins(
                        child, childSpec, 0,
                        heightMeasureSpec, 0
                    )
                    lp = child.layoutParams as MarginLayoutParams
                    childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
                    childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin
                }
            }
            if (lineViewList.isEmpty()) {
                lineMaxHeight = childHeight
            }
            lineViewList.add(child)
            lineTotalWidth += childWidth
        }
        if (!lineViewList.isEmpty()) {
            mAllViewIndex.add(childCount)
            mLineHeightList.add(lineMaxHeight)
            mTotalWidth = mTotalWidth.coerceAtLeast(lineTotalWidth)
            mTotalHeight += lineMaxHeight
        }

        val width = resolveSize(mTotalWidth, widthMeasureSpec)
        val height = resolveSize(mTotalHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onLayout(
        changed: Boolean,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) {
        println("onLayout: changed = ${changed}, l = $l, t = $t, r = $r, b = $b")
        if (!changed || childCount == 0) return

        val count = childCount
        var k = 0
        var startLeft = paddingLeft
        var lineMaxHeightHalf = mLineHeightList[k] / 2
        var startTop = paddingTop + lineMaxHeightHalf
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue
            if (i >= mAllViewIndex[k]) { // 一行结束
                startTop += lineMaxHeightHalf // 前一行加上后一半高度
                k++
                startLeft = paddingLeft
                lineMaxHeightHalf = mLineHeightList[k] / 2
                startTop += lineMaxHeightHalf
            }
            val lp = child.layoutParams as MarginLayoutParams
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            val childHeightHalf = childHeight / 2
            startLeft += lp.leftMargin
            child.layout(
                startLeft, startTop - childHeightHalf,
                startLeft + childWidth, startTop + childHeightHalf
            )
            startLeft += childWidth + lp.rightMargin
        }

//        val size = mAllView.size
//        var startTop = paddingTop
//        for (i in 0 until size) {
//            val lineViewList = mAllView[i]
//            var startLeft = paddingLeft
//            val lineMaxHeightHalf = mLineHeightList[i]/2
//            startTop += lineMaxHeightHalf
//
//            val lineSize = lineViewList.size
//            for (j in 0 until lineSize) {
//                val child = lineViewList[j]
//                val lp = child.layoutParams as MarginLayoutParams
//                val childWidth = child.measuredWidth
//                val childHeight = child.measuredHeight
//                val childHeightHalf = childHeight / 2
//                startLeft += lp.leftMargin
//                child.layout(
//                    startLeft, startTop - childHeightHalf,
//                    startLeft + childWidth, startTop + childHeightHalf
//                )
//                startLeft += childWidth + lp.rightMargin
//            }
//            startTop += lineMaxHeightHalf
//        }
    }
}