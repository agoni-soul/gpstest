package com.soul.bleSDK.manager

import com.soul.bean.BleScanResult
import com.soul.bleSDK.interfaces.IBleConnectCallback
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