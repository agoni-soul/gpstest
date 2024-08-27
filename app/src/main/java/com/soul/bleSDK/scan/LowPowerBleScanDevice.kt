package com.soul.bleSDK.scan

import com.soul.bleSDK.manager.BleScanManager


/**
 *     author : yangzy33
 *     time   : 2024-08-20
 *     desc   :
 *     version: 1.0
 */
class LowPowerBleScanDevice: BaseBleScanDevice() {
    override fun startScan(tag: String?) {
        tag ?: return
        BleScanManager.getInstance()?.startScan(tag, mBleScanCallbackMap[tag])
    }

    override fun isScanning(tag: String?): Boolean =
        BleScanManager.getInstance()?.isSubScanning(tag) ?: false

    override fun stopScan(tag: String?) {
        BleScanManager.getInstance()?.stopScan(tag)
    }
}