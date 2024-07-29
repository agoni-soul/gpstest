package com.soul

import android.app.Application
import android.util.Log
import com.soul.log.DOFLogUtil


/**
 *     author : yangzy33
 *     time   : 2024-05-17
 *     desc   :
 *     version: 1.0
 */
class SoulApplication : Application() {
    private val TAG = javaClass.simpleName

    companion object {
        var application: Application? = null
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        application = this
        initComponents()
        DOFLogUtil.init()
    }

    private fun initComponents() {
    }
}