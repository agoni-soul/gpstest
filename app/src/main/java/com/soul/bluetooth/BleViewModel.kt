package com.soul.bluetooth

import android.app.Application
import android.bluetooth.BluetoothAdapter
import com.soul.base.BaseViewModel


/**
 *     author : yangzy33
 *     time   : 2024-07-04
 *     desc   :
 *     version: 1.0
 */
class BleViewModel(mApplication: Application): BaseViewModel(mApplication) {
    var bluetoothAdapter: BluetoothAdapter? = null
        private set

    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    fun startDiscovery() {
        bluetoothAdapter?.startDiscovery()
    }

    fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }
}