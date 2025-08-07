package com.soul

//import com.squareup.leakcanary.LeakCanary
//import com.tencent.mmkv.MMKV
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.soul.log.DOFLogUtil
import com.soul.pluincore.HookUtils
import com.soul.pluincore.PluginManager
import java.io.File
import java.lang.reflect.Field


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

    private var mResources: Resources? = null;

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        application = this
//        LeakCanary.install(this)
//        MMKV.initialize(this);
        initComponents()
        DOFLogUtil.init()
        val pluginManager = PluginManager.getInStance(this)
        pluginManager.init()
        try {
            mResources = pluginManager.loadResources()
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        logger?.info("Initializing log4j") ?: Log.d(TAG, "init log4j fail")
//        initLogger()
    }

    private fun initLogger() {
        val logConfigFile = File(filesDir, "log4j2.xml")
        System.setProperty("log4j.configurationFile", logConfigFile.absolutePath)
    }

    private fun initComponents() {
    }

    override fun getResources(): Resources {
        return mResources ?: super.getResources()
    }
}