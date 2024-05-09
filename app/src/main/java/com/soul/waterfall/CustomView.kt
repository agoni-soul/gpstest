package com.soul.waterfall

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-03-14
 *     desc   :
 *     version: 1.0
 */
class CustomView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {

    private var paint: Paint = Paint()

    private val rectList: MutableList<Rect> = mutableListOf()

    constructor(context: Context): this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    fun setTransparentArea(left: Int, top: Int, right: Int, bottom: Int) {
        val dm = context.resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        val rectLeft = Rect(0, top, left, bottom)
        val rectTop = Rect(0, 0, width, top)
        val rectRight = Rect(right, top, width, bottom)
        val rectBottom = Rect(0, bottom, width, height)
        rectList.add(rectLeft)
        rectList.add(rectTop)
        rectList.add(rectRight)
        rectList.add(rectBottom)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = context.resources.getColor(R.color.gray_990c0c0c)
        Color.TRANSPARENT
        for (rect in rectList) {
            canvas?.drawRect(rect, paint)
        }
    }
}