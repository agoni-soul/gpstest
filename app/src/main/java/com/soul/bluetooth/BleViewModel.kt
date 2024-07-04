package com.soul.bluetooth

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.soul.base.BaseViewModel


/**
 *     author : yangzy33
 *     time   : 2024-07-04
 *     desc   :
 *     version: 1.0
 */
class BleViewModel(mApplication: Application): BaseViewModel(mApplication) {
    private var mBluetoothAdapter: BluetoothAdapter? = null

    init {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    fun startDiscovery() {
        if (ActivityCompat.checkSelfPermission(
                mApplication,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mBluetoothAdapter?.startDiscovery()
    }

    fun stopDiscovery() {
        if (ActivityCompat.checkSelfPermission(
                mApplication,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mBluetoothAdapter?.cancelDiscovery()
    }
}