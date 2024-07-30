package com.soul.bleSDK.utils

import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log

/**
 *     author : yangzy33
 *     time   : 2024-07-23
 *     desc   :
 *     version: 1.0
 */
object BleUtils {
    private val TAG = this.javaClass.simpleName

    /**
     * 打开手机蓝牙
     *
     * @return true 表示打开成功
     */
    fun enable(context: Context): Boolean {
        val bleManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return if (!bleManager.adapter.isEnabled) {
            //若未打开手机蓝牙，则会弹出一个系统的是否打开/关闭蓝牙的对话框，禁止或者未处理返回false，允许返回true
            //若已打开手机蓝牙，直接返回true
            val enableState: Boolean = bleManager.adapter.enable()
            Log.d(TAG, "（用户操作）手机蓝牙是否打开成功：$enableState")
            enableState
        } else true
    }
}