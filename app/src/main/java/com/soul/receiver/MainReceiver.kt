package com.soul.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.widget.Toast

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/05/31
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class MainReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 监听wifi的打开与关闭，与wifi的连接无关
        if (WifiManager.WIFI_STATE_CHANGED_ACTION == intent.action) {
            val wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)
            when (wifiState) {
                WifiManager.WIFI_STATE_ENABLED ->
                    Toast.makeText(context, "open wifi", Toast.LENGTH_SHORT).show()
                WifiManager.WIFI_STATE_DISABLING ->
                    Toast.makeText(context, "close wifi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
