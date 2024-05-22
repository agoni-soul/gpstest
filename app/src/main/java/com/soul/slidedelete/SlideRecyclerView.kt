package com.soul.slidedelete

import android.content.Context
import android.util.AttributeSet
import android.view.VelocityTracker
import androidx.recyclerview.widget.RecyclerView


/**
 *     author : yangzy33
 *     time   : 2024-05-22
 *     desc   :
 *     version: 1.0
 */
class SlideRecyclerView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): RecyclerView(context, attrs, defStyleAttr) {
    private val TAG = javaClass.simpleName
    companion object {
        private val INVALID_POSITION = -1
        private val INVALID_CHILD_WIDTH = -1
        private val SNAP_VELOCITY = 600
    }

    private var mVelocityTracker: VelocityTracker? = null
    private var mTouchSlop: Int = 0

    constructor(context: Context): this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)


}