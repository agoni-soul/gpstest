package com.soul.bleSDK.scan

import com.soul.bleSDK.manager.BleScanManager


/**
 *     author : yangzy33
 *     time   : 2024-08-20
 *     desc   :
 *     version: 1.0
 */
class ClassicBleScanDevice: BaseBleScanDevice() {

    override fun startScan(tag: String?) {
        mIsScanning = true
        BleScanManager.getInstance()?.startDiscovery()
    }

    override fun stopScan(tag: String?) {
        mIsScanning = false
        BleScanManager.getInstance()?.cancelDiscovery()
    }
}