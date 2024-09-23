package com.soul.util

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.provider.Settings

/**
 *
 * @author : haha
 * @date   : 2024-09-23
 * @desc   : 屏幕旋转监听
 *
 */
class ScreenRotationUtils {

    private var mRotationObserver: RotationObserver? = null

    fun init(context: Context?, handler: Handler, action: (Boolean) -> Unit) {
        context ?: return
        mRotationObserver = RotationObserver(context, handler, action)
    }

    fun registerReceiver(context: Context?) {
        mRotationObserver?.startObserver()
    }

    fun unregisterReceiver(context: Context?) {
        mRotationObserver?.stopObserver()
    }

    class RotationObserver(context: Context, handler: Handler, action: ((Boolean) -> Unit)) : ContentObserver(handler) {
        private val mResolver: ContentResolver by lazy { context.contentResolver }
        private val mAction = action

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            mAction.invoke(selfChange)
        }

        fun startObserver() {
            mResolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
                false,
                this
            )
        }

        fun stopObserver() {
            mResolver.unregisterContentObserver(this)
        }
    }

    interface RotationCallback {
        fun onChange(selfChange: Boolean)
    }
}