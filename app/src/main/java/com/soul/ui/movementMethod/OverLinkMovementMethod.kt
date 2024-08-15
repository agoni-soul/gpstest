package com.soul.ui.movementMethod

import android.text.NoCopySpan
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.view.MotionEvent
import android.widget.TextView


/**
 *     author : yangzy33
 *     time   : 2024-08-15
 *     desc   :
 *     version: 1.0
 */
class OverLinkMovementMethod: LinkMovementMethod() {
    companion object {
        var canScroll = false
        private var sInstance: OverLinkMovementMethod? = null
        private val FROM_BELOW = NoCopySpan.Concrete()

        fun getInstance(): MovementMethod {
            if (sInstance == null) {
                sInstance = OverLinkMovementMethod()
            }
            return sInstance!!
        }
    }

    override fun onTouchEvent(widget: TextView?, buffer: Spannable?, event: MotionEvent?): Boolean {
        val action = event?.action
        if (action == MotionEvent.ACTION_MOVE) {
            if (!canScroll) {
                return true
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }
}