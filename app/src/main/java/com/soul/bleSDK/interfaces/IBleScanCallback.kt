package com.soul.bleSDK.interfaces

import com.soul.bean.BleScanResult
import com.soul.bleSDK.constants.ScanSettings

interface IBleScanCallback {
    fun onBatchScanResults(results: MutableList<BleScanResult>?)
    fun onScanResult(@ScanSettings callbackType:Int, bleScanResult: BleScanResult?)
    fun onScanFailed(errorCode: Int)
}