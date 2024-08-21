package com.soul.bleSDK.interfaces

import com.soul.bean.BleScanResult
import com.soul.bleSDK.constants.ScanSettingType

interface IBleScanCallback {
    fun onBatchScanResults(results: MutableList<BleScanResult>?)
    fun onScanResult(@ScanSettingType callbackType: Int, bleScanResult: BleScanResult?)
    fun onScanFailed(errorCode: Int)
}