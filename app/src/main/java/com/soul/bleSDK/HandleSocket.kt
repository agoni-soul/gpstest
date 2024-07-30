package com.soul.bleSDK

import android.bluetooth.BluetoothSocket
import com.soul.bleSDK.interfaces.BaseBleListener
import com.soul.bleSDK.threads.WriteThread
import com.soul.bleSDK.utils.close
import com.soul.bluetooth.ReadThread
import java.io.Closeable


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
class HandleSocket(private val socket: BluetoothSocket?): Closeable {
    companion object {
        private val TAG = HandleSocket::class.java.simpleName
    }

    private lateinit var mReadThread: ReadThread
    private lateinit var mWriteThread: WriteThread

    fun start(
        readListener: BaseBleListener?,
        writeListener: BaseBleListener?
    ) {
        mReadThread = ReadThread(socket, readListener)
        mReadThread.start()
        mWriteThread = WriteThread(socket, writeListener)
    }

    fun sendMsg(msg: String?) {
        mWriteThread.sendMsg(msg)
    }

    override fun close() {
        mReadThread.cancel()
        mWriteThread.cancel()
        close(socket)
    }
}

