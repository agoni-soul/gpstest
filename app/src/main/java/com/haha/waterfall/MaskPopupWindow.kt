package com.haha.waterfall

import android.content.Context
import android.view.LayoutInflater
import android.widget.PopupWindow
import com.haha.hahalearn.R


/**
 *     author : yangzy33
 *     time   : 2024-03-07
 *     desc   :
 *     version: 1.0
 */
class MaskPopupWindow(
    mContext: Context
): PopupWindow() {

    init {
        contentView = LayoutInflater.from(mContext).inflate(R.layout.popupwindow_waterfall_mask, null, false)
        isFocusable = true
        isTouchable = true
        isOutsideTouchable = true
        isSplitTouchEnabled = false

        width = 320
        height = 50
    }
}
