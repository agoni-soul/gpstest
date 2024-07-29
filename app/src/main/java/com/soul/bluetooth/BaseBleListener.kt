package com.soul.bluetooth

import android.bluetooth.BluetoothSocket
import java.io.Closeable


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
interface BaseBleListener {
    open fun onSendMsg(socket: BluetoothSocket?, msg: String){}
    fun onFail(error: String)
}

fun close(vararg closeable: Closeable?) {
    closeable?.forEach { obj -> obj?.close() }
}