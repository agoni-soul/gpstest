package com.soul.bleSDK.scan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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
    fun getBleBondedDevices(): Set<BluetoothDevice>?
    fun startScan(tag: String? = null)
    fun stopScan(tag: String? = null)
    fun isScanning(tag: String? = null): Boolean
    fun setCallback(tag: String, callback: IBleScanCallback?)
}