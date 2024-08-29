package com.soul

import android.app.Application
import android.util.Log

/**
 *
 * @author : haha
 * @date   : 2024-08-29
 * @desc   : bleSDK 底层的Appliance
 * @version: 1.0
 *
 */
class BleSDKApplication : Application() {
    private val TAG = javaClass.simpleName

    companion object {
        var application: Application? = null
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        application = this
        initComponents()
    }

    private fun initComponents() {
    }
}