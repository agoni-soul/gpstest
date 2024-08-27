package com.soul.bleSDK

import android.bluetooth.BluetoothSocket
import com.soul.bleSDK.interfaces.BaseBleListener
import com.soul.bleSDK.threads.WriteThread
import com.soul.bleSDK.threads.ReadThread
import com.soul.bleSDK.utils.close
import java.io.Closeable


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
class HandleSocket(private val bleSocket: BluetoothSocket?): Closeable {
    private val TAG: String = javaClass.simpleName

    private var mReadThread: ReadThread? = null
    private var mWriteThread: WriteThread? = null

    fun initReadThread(readListener: BaseBleListener?) {
        mReadThread = ReadThread(bleSocket, readListener)
    }

    fun initWriteThread(writeListener: BaseBleListener?) {
        mWriteThread = WriteThread(bleSocket, writeListener)
    }

    fun startReadMessage() {
        mReadThread?.startReadMessage()
    }

    fun sendMessage(msg: String?) {
        mWriteThread?.sendMessage(msg)
    }

    override fun close() {
        close(mReadThread, mWriteThread, bleSocket)
    }
}

