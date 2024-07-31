package com.soul.bleSDK.interfaces

import com.soul.bean.BleScanResult
import com.soul.bleSDK.exceptions.BleErrorException


/**
 *     author : yangzy33
 *     time   : 2024-07-31
 *     desc   :
 *     version: 1.0
 */
interface IBleConnectCallback {
    fun onStart()
    fun onConnected(bleScanResult: BleScanResult?)
    fun onFail(e: BleErrorException?)
    fun close()
}