package com.soul.util

import android.content.Context
import android.content.Intent

/**
 *
 * @author : haha
 * @date   : 2024-09-23
 * @desc   : 广播监听回调
 *
 */
interface BroadReceiverCallback {
    fun onReceiveCallback(context: Context, intent: Intent)
}