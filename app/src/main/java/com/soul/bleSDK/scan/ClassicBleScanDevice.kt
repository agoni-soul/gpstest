package com.soul.bleSDK.scan

import com.soul.bleSDK.manager.BleScanManager


/**
 *     author : haha
 *     time   : 2024-08-20
 *     desc   :
 *     version: 1.0
 */
class ClassicBleScanDevice: BaseBleScanDevice() {

    override fun startScan(tag: String?) {
        BleScanManager.getInstance()?.startDiscovery()
    }

    override fun isScanning(tag: String?): Boolean {
        mIsScanning = BleScanManager.getInstance()?.isClassicScanning() ?: false
        return mIsScanning
    }

    override fun stopScan(tag: String?) {
        super.stopScan(tag)
        if (isScanning(tag)) {
            BleScanManager.getInstance()?.cancelDiscovery()
        }
    }
}