package com.haha.blesdk.interfaces

import com.haha.blesdk.bean.BleScanResult
import com.haha.blesdk.exceptions.BleErrorException


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