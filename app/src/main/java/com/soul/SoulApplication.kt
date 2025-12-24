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
import leakcanary.AppWatcher
import leakcanary.LeakCanary
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
        leakCanaryConfig()
//        LeakCanary.install(this)
//        MMKV.initialize(this);
        initComponents()
        DOFLogUtil.init()
//        val pluginManager = PluginManager.getInStance(this)
//        pluginManager.init()
//        try {
//            mResources = pluginManager.loadResources()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        logger?.info("Initializing log4j") ?: Log.d(TAG, "init log4j fail")
//        initLogger()
    }

    private fun leakCanaryConfig() {
        //App 处于前台时检测保留对象的阈值，默认是 5
        LeakCanary.config = LeakCanary.config.copy(retainedVisibleThreshold = 3)
        //自定义要检测的保留对象类型，默认监测 Activity，Fragment，FragmentViews 和 ViewModels
        AppWatcher.config= AppWatcher.config.copy(watchFragmentViews = false)
        //隐藏泄漏显示活动启动器图标，默认为 true
        LeakCanary.showLeakDisplayActivityLauncherIcon(false)
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