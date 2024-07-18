package com.soul.waterfall

import android.content.Context
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-03-07
 *     desc   :
 *     version: 1.0
 */
class MaskPopupWindow(
    private val mContext: Context
): PopupWindow() {
    private val mWindowManager: WindowManager by lazy {
        mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private var mMaskView: CustomView? = null

    init {
        contentView = LayoutInflater.from(mContext).inflate(R.layout.popupwindow_waterfall_mask, null, false)
        isFocusable = true
        isTouchable = true
        isOutsideTouchable = true
        isSplitTouchEnabled = false

        width = 320
        height = 50
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        anchor?.let {
            addMask(it.windowToken)
        }
        super.showAsDropDown(anchor, xoff, yoff, gravity)
    }

    private fun addMask(token: IBinder) {
        val wl = WindowManager.LayoutParams()
        wl.width = WindowManager.LayoutParams.MATCH_PARENT
        wl.height = WindowManager.LayoutParams.MATCH_PARENT
        wl.format = PixelFormat.TRANSLUCENT //不设置这个弹出框的透明遮罩显示为黑色
        wl.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL//该Type描述的是形成的窗口的层级关系
        wl.token = token//获取当前Activity中的View中的token,来依附Activity
        mMaskView = CustomView(mContext)
        mMaskView?.let {
            val dm = mContext.resources.displayMetrics
//            it.setTransparentArea(DpToPxTransfer.dp2px(mContext, 16), DpToPxTransfer.dp2px(mContext, 100), dm.widthPixels - DpToPxTransfer.dp2px(mContext, 16), DpToPxTransfer.dp2px(mContext, 145))
            it.fitsSystemWindows = false
            it.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    removeMask()
                    true
                } else {
                    false
                }
            }
            /**
             * 通过WindowManager的addView方法创建View，产生出来的View根据WindowManager.LayoutParams属性不同，效果也就不同了。
             * 比如创建系统顶级窗口，实现悬浮窗口效果！
             */
            mWindowManager.addView(it, wl)
        }
    }

    private fun removeMask() {
        if (mMaskView != null) {
            mWindowManager.removeView(mMaskView)
            mMaskView = null
        }
    }

    override fun dismiss() {
        removeMask()
        super.dismiss()
    }
}