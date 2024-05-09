package com.soul.waterfall

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.soul.gpstest.R
import com.soul.log.DOFLogUtil


/**
 *     author : yangzy33
 *     time   : 2024-02-20
 *     desc   :
 *     version: 1.0
 */
class WaterFallLayout(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) :
    ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private val TAG = this.javaClass.simpleName

    private val allViews: MutableList<MutableList<View>> = mutableListOf()

    private val horizontalSpace: Int =
        resources.getDimensionPixelOffset(R.dimen.item_horizontal_interval)

    private val verticalSpace: Int =
        resources.getDimensionPixelOffset(R.dimen.item_vertical_interval)

    private val lineHeights: MutableList<Int> = mutableListOf()

    constructor(context: Context) : this(context, null, 0, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(TAG, "haha: onMeasure")
        // 会测量次
        allViews.clear()
        lineHeights.clear()

        // 保存每一行的view
        var everyLineViews = mutableListOf<View>()
        // 记录每一行当前的宽度，判断是否换行
        var curLineHasUsedWidth: Int = paddingStart + paddingEnd
        // 父布局给定的宽度
        val selfWidth: Int = MeasureSpec.getSize(widthMeasureSpec)
        // 父布局给定的高度
        val selfHeight: Int = MeasureSpec.getSize(heightMeasureSpec)
        // 测量需要的宽度（如果用户在布局里对ZSFlowLayout的宽设置了wrap_content 就会用到这个）
        var selfNeedWidth = 0
        // 测量需要的高度（如果用户在布局里对ZSFlowLayout的高设置了wrap_content 就会用到这个）
        var selfNeedHeight = paddingBottom + paddingTop

        var curLineHeight = 0

        // 第一步 先测量子view 核心系统方法是[View.measure]
        // (1)因为子view有很多，所以循环遍历执行
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            if (childView.visibility == GONE) {
                continue
            }
            // 测量view之前，先把测量需求的参数准备好 通过[ViewGroup#getChildMeasureSpec] 获取子View的MeasureSpec信息
            val childWidthMeasureSpec = getChildMeasureSpec(
                widthMeasureSpec,
                paddingStart + paddingEnd,
                childView.layoutParams.width
            )
            val childHeightMeasureSpec = getChildMeasureSpec(
                heightMeasureSpec,
                paddingTop + paddingBottom,
                childView.layoutParams.height
            )
            // 调用子View的measure()对子View进行测量
            childView.measure(childWidthMeasureSpec, childHeightMeasureSpec)

            // 测量之后就能拿到子view的宽高，保存起来用于判断是否换行，以及需要的高度
            val measuredWidth = childView.measuredWidth
            Log.d(TAG, "onMeasure measureWidth = $measuredWidth, width = ${childView.width}")
            val measuredHeight = childView.measuredHeight
            Log.d(TAG, "onMeasure measuredHeight = $measuredHeight, height = ${childView.height}")

            // 按行保存View 保存之前判断是否需要换行，如果需要则保存在下一行的list里面
            if (curLineHasUsedWidth + measuredWidth > selfWidth) { // 需要换行，先记录换行之前的数据
                lineHeights.add(curLineHeight)
                selfNeedHeight += curLineHeight + verticalSpace
                allViews.add(everyLineViews)

                // 再处理当前要换行的view相关数据
                curLineHeight = measuredHeight
                everyLineViews = mutableListOf()
                curLineHasUsedWidth = paddingStart + paddingEnd + measuredWidth + horizontalSpace
            } else {
                // 每一行的高度是这一行view中最高的那个
                curLineHeight = curLineHeight.coerceAtLeast(measuredHeight)
                curLineHasUsedWidth += measuredWidth + horizontalSpace
            }
            everyLineViews.add(childView)
            selfNeedWidth = selfNeedWidth.coerceAtLeast(curLineHasUsedWidth)

            // 处理最后一行
            if (i == childCount - 1) {
                curLineHeight = curLineHeight.coerceAtLeast(measuredHeight)
                allViews.add(everyLineViews)
                selfNeedHeight += curLineHeight
                lineHeights.add(curLineHeight)
            }
        }

        // 第二步 测量自己
        // 根据父类传入的尺寸规则 widthMeasureSpec、heightMeasureSpec 获取当前自身应该遵守的布局模式
        // 以widthMeasureSpec 为例说明下，这个是父类传入的，那父类是如何按照什么规则生成的widthMeasureSpec呢?
        // 父类会结合自身的情况，并且结合子view的情况(子类的宽是match_parent、wrap_content、还是写死的值)来生成
        // 生成的具体逻辑 请见： ViewGroup#getChildMeasureSpec()
        // (1)获取父类传过来的值
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        // (2)根据模式来判断最终的宽高
        val widthResult = if (widthMode == MeasureSpec.EXACTLY) selfWidth else selfNeedWidth
        val heightResult = if (heightMode == MeasureSpec.EXACTLY) selfHeight else selfNeedHeight

        // (3)设置自身的测量结果
        setMeasuredDimension(widthResult, heightResult)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        Log.d(TAG, "haha: onLayout")
        // 设置所有view的位置
        var curT = paddingTop
        for (i in allViews.indices) {
            val everyLineView = allViews[i]
            // 记录每一行view的当前距离父布局左侧的位置，初始值为父布局的paddingStart
            var curS = paddingStart
            if (i != 0) {
                curT += lineHeights[i - 1] + verticalSpace
            }
            for (j in everyLineView.indices) {
                val view = everyLineView[j]
                Log.d(TAG, "onLayout measureWidth = ${view.measuredWidth}, width = ${view.width}")
                Log.d(TAG, "onLayout measuredHeight = ${view.measuredHeight}, height = ${view.height}")
                if (view.visibility == GONE) continue
                val end = curS + view.measuredWidth
                val bottom = curT + view.measuredHeight
                view.layout(curS, curT, end, bottom)
                // 为下一个View做准备
                curS += view.measuredWidth + horizontalSpace
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Log.d(TAG, "haha: onDraw")
    }
}