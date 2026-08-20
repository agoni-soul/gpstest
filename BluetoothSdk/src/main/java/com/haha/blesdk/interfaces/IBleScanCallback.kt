package com.haha.blesdk.interfaces

import com.haha.blesdk.bean.BleScanResult
import com.haha.blesdk.constants.ScanSettingType


/**
 *     author : haha
 *     time   : 2024-07-31
 *     desc   :
 *     version: 1.0
 */
interface IBleScanCallback {
    fun onBatchScanResults(results: MutableList<BleScanResult>?)
    fun onScanResult(@ScanSettingType callbackType: Int, bleScanResult: BleScanResult?)
    fun onScanFailed(errorCode: Int)
}