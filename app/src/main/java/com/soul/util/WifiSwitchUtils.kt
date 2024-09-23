package com.soul.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

/**
 *
 * @author : haha
 * @date   : 2024-09-23
 * @desc   : wifi广播监听
 *
 */
class WifiSwitchUtils {
    val WIFI_STATE_CHANGED = "android.net.wifi.WIFI_STATE_CHANGED"

    private val mWifiIntentFilter: IntentFilter by lazy {
        IntentFilter()
    }

    private val callbackActions: MutableList<String> by lazy {
        mutableListOf()
    }

    private val mWifiBroadReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                context ?: return
                intent ?: return
                mBroadReceiverCallback?.onReceiveCallback(context, intent)
            }
        }
    }

    private var mBroadReceiverCallback: BroadReceiverCallback? = null

    fun addWifiStateChanged() {
        addAction(WIFI_STATE_CHANGED)
    }

    fun addAction(action: String?) {
        action ?: return
        mWifiIntentFilter.addAction(action)
    }

    fun setBroadReceiverCallback(callback: BroadReceiverCallback?) {
        mBroadReceiverCallback = callback
    }

    fun registerReceiver(context: Context?) {
        context ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(mWifiBroadReceiver, mWifiIntentFilter,
                Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(mWifiBroadReceiver, mWifiIntentFilter)
        }
    }

    fun unregisterReceiver(context: Context?) {
        context ?: return
        context.unregisterReceiver(mWifiBroadReceiver)
    }
}