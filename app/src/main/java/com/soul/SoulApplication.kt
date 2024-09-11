package com.soul

import android.app.Application
import android.util.Log
import com.soul.log.DOFLogUtil
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File


/**
 *     author : yangzy33
 *     time   : 2024-05-17
 *     desc   :
 *     version: 1.0
 */
class SoulApplication : Application() {
    private val TAG = javaClass.simpleName
//    private val logger: Logger? = LogManager.getLogger(this.javaClass)

    companion object {
        var application: Application? = null
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        application = this
        initComponents()
        DOFLogUtil.init()
//        logger?.info("Initializing log4j") ?: Log.d(TAG, "init log4j fail")
//        initLogger()
    }

    private fun initLogger() {
        val logConfigFile = File(filesDir, "log4j2.xml")
        System.setProperty("log4j.configurationFile", logConfigFile.absolutePath)
    }

    private fun initComponents() {
    }
}