package com.soul.bleSDK.scan

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import com.soul.bleSDK.interfaces.IBleScanCallback
import com.soul.bleSDK.manager.BleScanManager


/**
 *     author : yangzy33
 *     time   : 2024-08-20
 *     desc   :
 *     version: 1.0
 */
class LowPowerBleScanDevice : BaseBleScanDevice() {
    override fun startScan(tag: String?) {
        tag ?: return
        BleScanManager.getInstance()?.startScan(tag, mBleScanCallbackMap[tag])
    }

    fun startScan(
        tag: String,
        scanDurationTime: Long,
        scanFilters: MutableList<ScanFilter>? = null,
        scanSettings: ScanSettings = ScanSettings.Builder().build()
    ) {
        mIsScanning = true
        BleScanManager.getInstance()
            ?.startScan(tag, scanDurationTime, scanFilters, scanSettings, mBleScanCallbackMap[tag])
    }

    override fun isScanning(tag: String?): Boolean {
        mIsScanning = BleScanManager.getInstance()?.isSubScanning(tag) ?: false
        return mIsScanning
    }

    override fun stopScan(tag: String?) {
        super.stopScan(tag)
        if (isScanning(tag)) {
            BleScanManager.getInstance()?.stopScan(tag)
        }
    }
}