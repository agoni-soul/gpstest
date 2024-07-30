package com.soul.bleSDK.interfaces

import android.bluetooth.BluetoothDevice
import com.soul.bleSDK.BleListener

interface IBleCallback {
    fun sendMsg(msg: String?)
    fun close()
    fun start(bluetoothDevice: BluetoothDevice?)
    fun setReadListener(readListener: BleListener?)
    fun setWriteListener(writeListener: BaseBleListener?)
}