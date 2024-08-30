package com.soul.blesdk.manager

import android.bluetooth.BluetoothSocket
import com.soul.blesdk.HandleSocket
import com.soul.blesdk.interfaces.BaseBleListener
import com.soul.blesdk.interfaces.BleListener
import java.io.Closeable


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
): Closeable {
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

    override fun close() {
        mHandleSocket?.close()
    }
}