package com.soul.blesdk.interfaces

import android.bluetooth.BluetoothSocket


/**
 *     author : haha
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
interface BleListener: BaseBleListener {
    fun onStart()
    fun onReceiveData(socket: BluetoothSocket?, msg: String?)
    fun onConnected(msg: String?) {}
}