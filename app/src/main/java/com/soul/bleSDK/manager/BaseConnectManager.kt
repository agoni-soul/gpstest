package com.soul.bleSDK.manager

import com.soul.bean.BleScanResult
import java.io.Closeable


/**
 *     author : haha
 *     time   : 2024-07-31
 *     desc   :
 *     version: 1.0
 */
abstract class BaseConnectManager: Closeable, BaseBleManager() {
    abstract fun connect(bleScanResult: BleScanResult?)
}