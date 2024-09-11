package com.soul.blesdk.interfaces

import com.soul.blesdk.bean.BleScanResult
import com.soul.blesdk.exceptions.BleErrorException


/**
 *     author : haha
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