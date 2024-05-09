package com.soul.waterfall

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView


/**
 *     author : yangzy33
 *     time   : 2024-03-11
 *     desc   :
 *     version: 1.0
 */
class CustomRecyclerView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val height = context.resources.displayMetrics.heightPixels
        val maxHeight = height * 7 / 15
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)

        super.onMeasure(widthSpec, heightMeasureSpec)
    }
}