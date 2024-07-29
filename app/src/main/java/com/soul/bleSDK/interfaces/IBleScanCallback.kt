package com.soul.bleSDK.interfaces

import com.soul.bean.BleScanResult

interface IBleScanCallback {
    fun onBatchScanResults(results: MutableList<BleScanResult>?)
    fun onScanResult(callbackType:Int, result: BleScanResult?)
    fun onScanFailed(errorCode: Int)
}