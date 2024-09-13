package com.soul.blesdk.interfaces

import com.soul.blesdk.bean.BleScanResult
import com.soul.blesdk.constants.ScanSettingType


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