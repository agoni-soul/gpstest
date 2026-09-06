package com.haha

//import com.squareup.leakcanary.LeakCanary
//import com.tencent.mmkv.MMKV
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Looper
import android.util.Log
import com.haha.base.ActivityManager
import com.haha.hahalearn.BuildConfig
import com.haha.leakcanary.LeakCanaryInstaller
import com.haha.log.DOFLogUtil
import com.haha.main.timeMonitor.TimeMonitorConfig
import com.haha.main.timeMonitor.TimeMonitorManager
import com.haha.service.impl.service.ServiceLoader
import com.haha.servicerouter.core.DOFRouter
import java.io.File


/**
 *     author : yangzy33
 *     time   : 2024-05-17
 *     desc   :
 *     version: 1.0
 */
class HahaApplication : Application() {
    private val TAG = javaClass.simpleName
//    private val logger: Logger? = LogManager.getLogger(this.javaClass)

    companion object {
        var application: Application? = null

        @SuppressLint("StaticFieldLeak")
        private var mContext: Context? = null

        fun getContext(): Context? = mContext

        init {
            // Used to load the 'nativeTest' library on application startup.
            System.loadLibrary("HahaLearn")
        }
    }

    private var mResources: Resources? = null

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        mContext = this
        // 早于 TimeMonitor / ActivityManager，启动点与崩溃才能落盘
        DOFLogUtil.init(this)
        // 进程启动立刻注册，覆盖后续全部 Activity，替代 BaseActivity 内 ActivityCollector
        ActivityManager.init(this)
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
        initComponents()
        // debug LeakCanary 不挡 onCreate；release 为空实现。Idle 时首个 Activity 已 create，
        // AppWatcher 仍能在 onDestroy 时 watch。
        Looper.myQueue().addIdleHandler {
            LeakCanaryInstaller.install(this)
            false
        }
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

    private fun initLogger() {
        val logConfigFile = File(filesDir, "log4j2.xml")
        System.setProperty("log4j.configurationFile", logConfigFile.absolutePath)
    }

    private fun initComponents() {
        DOFRouter.openDebug()
        DOFRouter.init(this)
        ServiceLoader.init(this, BuildConfig.DEBUG)
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