package com.soul.bleSDK.manager

import android.bluetooth.BluetoothSocket
import com.soul.bleSDK.BleListener
import com.soul.bleSDK.HandleSocket
import com.soul.bleSDK.interfaces.BaseBleListener
import com.soul.bleSDK.utils.close


/**
 *     author : yangzy33
 *     time   : 2024-07-31
 *     desc   :
 *     version: 1.0
 */
class BleCommunicateManager(
    private val mBleSocket: BluetoothSocket?,
    private val readListener: BleListener? = null,
    private val writeListener: BaseBleListener? = null
) {
    private var mHandleSocket: HandleSocket? = null

    fun init() {
        //处理 socket 读写
        mHandleSocket = HandleSocket(mBleSocket).apply {
            initReadThread(readListener)
            initWriteThread(writeListener)
        }
    }

    fun startReadMessage() {
        mHandleSocket?.startReadMessage()
    }

    fun sendMessage(message: String?) {
        mHandleSocket?.sendMessage(message)
    }

    fun close() {
        mHandleSocket?.close()
    }
}