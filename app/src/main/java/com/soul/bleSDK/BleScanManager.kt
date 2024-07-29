package com.soul.bleSDK

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context

class BleScanManager(context: Context) {
    private var mBleAdapter: BluetoothAdapter? = null
    private var mBleA2dp: BluetoothA2dp? = null

    init {
        val bleManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager?
        mBleAdapter =  bleManager?.adapter?.apply {
            getProfileProxy(context, object: BluetoothProfile.ServiceListener {
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                    if (profile == BluetoothProfile.A2DP && proxy is BluetoothA2dp) {
                        mBleA2dp = proxy
                    }
                }

                override fun onServiceDisconnected(profile: Int) {
                    if (profile == BluetoothProfile.A2DP) {
                        mBleA2dp = null
                    }
                }

            }, BluetoothProfile.A2DP)
        }
    }

    fun startDiscovery() {
        mBleAdapter?.startDiscovery()
    }

    fun stopDiscovery() {
        mBleAdapter?.cancelDiscovery()
    }
}