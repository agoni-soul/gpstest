package com.soul.blesdk.manager

import com.soul.blesdk.bean.BleScanResult
import com.soul.blesdk.interfaces.IBleConnectCallback
import java.io.Closeable


/**
 *     author : haha
 *     time   : 2024-07-31
 *     desc   :
 *     version: 1.0
 */
abstract class BaseConnectManager: Closeable, BaseBleManager() {
    protected var mBleConnectCallback: IBleConnectCallback? = null

    abstract fun connect(bleScanResult: BleScanResult?)

    fun setConnectCallback(bleConnectCallback: IBleConnectCallback?) {
        mBleConnectCallback = bleConnectCallback
    }
}