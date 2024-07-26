package com.soul.bluetooth

import android.app.Application
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
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

    var bleA2dp: BluetoothA2dp? = null
        private set

    init {
        val bleManager = mApplication.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bleManager.adapter.apply {
            getProfileProxy(mApplication, object: BluetoothProfile.ServiceListener {
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                    if (profile == BluetoothProfile.A2DP && proxy is BluetoothA2dp) {
                        bleA2dp = proxy
                    }
                }

                override fun onServiceDisconnected(profile: Int) {
                    if (profile == BluetoothProfile.A2DP) {
                        bleA2dp = null
                    }
                }

            }, BluetoothProfile.A2DP)
        }

    }

    fun startDiscovery() {
        bluetoothAdapter?.startDiscovery()
    }

    fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }
}