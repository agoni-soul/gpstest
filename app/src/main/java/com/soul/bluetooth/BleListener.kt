package com.soul.bluetooth

import android.bluetooth.BluetoothSocket


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
interface BleListener: BaseBleListener {
    fun onStart()
    fun onReceiveData(socket: BluetoothSocket?, msg: String)
    open fun onConnected(msg: String) {}
}