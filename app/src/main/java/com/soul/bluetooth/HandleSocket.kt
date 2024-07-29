package com.soul.bluetooth

import android.bluetooth.BluetoothSocket


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
class HandleSocket(private val socket: BluetoothSocket?) {
    companion object {
        private val TAG = HandleSocket::class.java.simpleName
    }

    private lateinit var mReadThread: ReadThread
    private lateinit var mWriteThread: WriteThread

    fun start(
        readListener: BleListener?,
        writeListener: BaseBleListener?
    ) {
        mReadThread = ReadThread(socket, readListener)
        mReadThread.start()
        mWriteThread = WriteThread(socket, writeListener)
    }

    fun sendMsg(msg: String) {
        mWriteThread.sendMsg(msg)
    }

    fun cancel() {
        mReadThread.cancel()
        mWriteThread.cancel()
        close(socket)
    }
}

