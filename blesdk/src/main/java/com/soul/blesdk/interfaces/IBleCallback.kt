package com.soul.blesdk.interfaces

import android.bluetooth.BluetoothDevice


/**
 *     author : haha
 *     time   : 2024-07-31
 *     desc   :
 *     version: 1.0
 */
interface IBleCallback {
    fun sendMsg(msg: String?)
    fun close()
    fun start(bluetoothDevice: BluetoothDevice?)
    fun setReadListener(readListener: BleListener?)
    fun setWriteListener(writeListener: BaseBleListener?)
}