package com.soul.util

import android.content.Context
import android.util.TypedValue


/**
 *     author : yangzy33
 *     time   : 2024-03-14
 *     desc   :
 *     version: 1.0
 */
object DpToPxTransfer {

    /**
     * 将dp转换成对用的px
     */
    fun dp2px(context: Context, dp: Int): Int = TypedValue
        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()

    /**
     * 将sp转换成对用的px
     */
    fun sp2px(context: Context, sp: Int): Int = TypedValue
        .applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), context.resources.displayMetrics).toInt()
}