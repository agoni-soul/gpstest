package com.soul.bleSDK.interfaces

import android.bluetooth.BluetoothSocket


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
interface BaseBleListener {
    fun onSendMsg(socket: BluetoothSocket?, msg: String?){}
    fun onFail(error: String?)
}