package com.soul.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-03-13
 *     desc   :
 *     version: 1.0
 */
class WaterFallLayout(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val allViews: MutableList<MutableList<View>> = mutableListOf()

    private val horizontalSpace: Int =
        resources.getDimensionPixelOffset(R.dimen.item_horizontal_interval)

    private val verticalSpace: Int =
        resources.getDimensionPixelOffset(R.dimen.item_vertical_interval)

    private val lineHeights: MutableList<Int> = mutableListOf()

    constructor(context: Context) : this(context, null, 0, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        allViews.clear()
        lineHeights.clear()

        var everyLineViews = mutableListOf<View>()
        var curLineHasUsedWidth = paddingLeft + paddingRight
        val selfWidth: Int = MeasureSpec.getSize(widthMeasureSpec)
        val selfHeight: Int = MeasureSpec.getSize(heightMeasureSpec)
        var selfNeedWidth = 0
        var selfNeedHeight = paddingBottom + paddingTop
        var curLineHeight = 0

        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            if (childView.visibility == GONE) {
                continue
            }
            val childWidthMeasureSpec = getChildMeasureSpec(
                widthMeasureSpec,
                paddingLeft + paddingRight,
                childView.layoutParams.width
            )
            val childHeightMeasureSpec = getChildMeasureSpec(
                heightMeasureSpec,
                paddingTop + paddingBottom,
                childView.layoutParams.height
            )
            childView.measure(childWidthMeasureSpec, childHeightMeasureSpec)

            val measureWidth = childView.measuredWidth
            val measureHeight = childView.measuredHeight

            if (curLineHasUsedWidth + measureWidth > selfWidth) {
                lineHeights.add(curLineHeight)
                selfNeedHeight += curLineHeight + verticalSpace
                allViews.add(everyLineViews)

                curLineHeight = measureHeight
                everyLineViews = mutableListOf()
                everyLineViews.add(childView)
                curLineHasUsedWidth = paddingLeft + paddingRight + measureWidth
            } else {
                curLineHeight = curLineHeight.coerceAtLeast(measureHeight)
                everyLineViews.add(childView)
                curLineHasUsedWidth += measureWidth + horizontalSpace
            }
            selfNeedWidth = selfNeedWidth.coerceAtLeast(curLineHasUsedWidth)

            if (i == childCount - 1) {
                curLineHeight = curLineHeight.coerceAtLeast(measureHeight)
                allViews.add(everyLineViews)
                selfNeedHeight += curLineHeight
                lineHeights.add(curLineHeight)
            }
        }

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val widthResult = if (widthMode == MeasureSpec.EXACTLY) selfWidth else selfNeedWidth
        val heightResult = if (heightMode == MeasureSpec.EXACTLY) selfHeight else selfNeedHeight

        setMeasuredDimension(widthResult, heightResult)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var curT = paddingTop
        for (i in allViews.indices) {
            val everyLineView = allViews[i]
            var curS = paddingStart
            if (i != 0) {
                curT += lineHeights[i - 1] + verticalSpace
            }
            for (j in everyLineView.indices) {
                val childView = everyLineView[j]
                if (childView.visibility == GONE) continue
                val end = curS + childView.measuredWidth
                val bottom = curT + childView.measuredHeight
                childView.layout(curS, curT, end, bottom)
                curS += childView.measuredWidth + horizontalSpace
            }
        }
    }
}