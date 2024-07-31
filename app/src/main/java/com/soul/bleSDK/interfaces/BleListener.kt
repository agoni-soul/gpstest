package com.soul.bleSDK

import android.bluetooth.BluetoothSocket
import com.soul.bleSDK.interfaces.BaseBleListener


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
interface BleListener: BaseBleListener {
    fun onStart()
    fun onReceiveData(socket: BluetoothSocket?, msg: String?)
    fun onConnected(msg: String?) {}
}