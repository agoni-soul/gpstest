package com.soul.bleSDK.interfaces

import com.soul.bean.BleScanResult

interface IBleScanCallback {
    fun onBatchScanResults(results: MutableList<BleScanResult>?)
    fun onScanResult(callbackType:Int, bleScanResult: BleScanResult?)
    fun onScanFailed(errorCode: Int)
}