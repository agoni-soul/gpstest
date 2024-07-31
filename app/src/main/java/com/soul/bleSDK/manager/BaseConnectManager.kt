package com.soul.bleSDK.manager

import com.soul.bean.BleScanResult


/**
 *     author : yangzy33
 *     time   : 2024-07-31
 *     desc   :
 *     version: 1.0
 */
abstract class BaseConnectManager {
    protected val TAG = this.javaClass::class.simpleName

    abstract fun connect(bleScanResult: BleScanResult?)

    abstract fun close()
}