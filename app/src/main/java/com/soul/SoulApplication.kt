package com.soul

//import com.squareup.leakcanary.LeakCanary
//import com.tencent.mmkv.MMKV
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.soul.log.DOFLogUtil
import com.soul.main.timeMonitor.TimeMonitorConfig
import com.soul.main.timeMonitor.TimeMonitorManager
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

        @SuppressLint("StaticFieldLeak")
        private var mContext: Context? = null

        fun getContext(): Context? = mContext

        init {
            // Used to load the 'nativeTest' library on application startup.
            System.loadLibrary("GPSTest")
        }
    }

    private var mResources: Resources? = null

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        mContext = this
        TimeMonitorManager.getInstance()
            .resetTimeMonitor(TimeMonitorConfig.TIME_MONITOR_ID_APPLICATION_START)
        TimeMonitorManager.getInstance()
            .getTimeMonitor(TimeMonitorConfig.TIME_MONITOR_ID_APPLICATION_START)
            .startMonitor()
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        TimeMonitorManager.getInstance()
            .getTimeMonitor(TimeMonitorConfig.TIME_MONITOR_ID_APPLICATION_START)
            .recodingTimeTag("ApplicationCreate")

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

    override fun onTerminate() {
        super.onTerminate()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }



    override fun getResources(): Resources {
        return mResources ?: super.getResources()
    }
}