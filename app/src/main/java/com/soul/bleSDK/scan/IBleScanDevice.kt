package com.soul.bleSDK.scan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import com.soul.bleSDK.interfaces.IBleScanCallback


/**
 *     author : yangzy33
 *     time   : 2024-08-20
 *     desc   :
 *     version: 1.0
 */
interface IBleScanDevice {
    fun getBluetoothManager(): BluetoothManager?
    fun getBluetoothAdapter(): BluetoothAdapter?
    fun startScan()
    fun stopScan()
    fun isScanning(): Boolean
    fun setCallback(callback: IBleScanCallback)
}