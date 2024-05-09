package com.soul.waterfall

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView

/**
 *     author : yangzy33
 *     time   : 2024-03-13
 *     desc   :
 *     version: 1.0
 */
class VerticalScrollBarDecoration: RecyclerView.ItemDecoration() {

    private val paint = Paint()

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val paddingTop = dp2px(parent.context, 16F).toFloat()
        val indicatorHeight = 168F
        val width = 6F
        val marginEnd = 0F

        val extent = parent.computeVerticalScrollExtent()
        val range = parent.computeVerticalScrollRange()
        val offset = parent.computeVerticalScrollOffset()

        val maxOffset = range - extent
        if (maxOffset > 0) {
            val startX = parent.width - marginEnd - width
            paint.isAntiAlias = true
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = width
//            paint.color = Color.parseColor("#FF03DAC5")
//            c.drawLine(startX, paddingTop, startX, parent.height - paddingTop, paint)
            paint.color = Color.parseColor("#FFE66A46")
            val ratio = offset.toFloat() / maxOffset.toFloat()
            val offsetY = ratio * (parent.height - 2 * paddingTop - indicatorHeight)
            c.drawLine(startX, paddingTop + offsetY, startX, paddingTop + indicatorHeight + offsetY, paint)

        }
    }

    private fun px2dp(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5F).toInt()
    }

    private fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5F).toInt()
    }
}