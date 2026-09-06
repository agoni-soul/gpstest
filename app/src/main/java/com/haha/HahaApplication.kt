package com.haha

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import com.haha.main.timeMonitor.TimeMonitorConfig
import com.haha.main.timeMonitor.TimeMonitorManager
import com.haha.startup.AppInitTable
import com.haha.startup.InitStage


/**
 *     author : yangzy33
 *     time   : 2024-05-17
 *     desc   :
 *     version: 1.0
 */
class HahaApplication : Application() {
    private val TAG = javaClass.simpleName

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
        AppInitTable.run(this, InitStage.ATTACH_BASE)
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        TimeMonitorManager.getInstance()
            .getTimeMonitor(TimeMonitorConfig.TIME_MONITOR_ID_APPLICATION_START)
            .recodingTimeTag("ApplicationCreate")

        application = this
        AppInitTable.run(this, InitStage.ON_CREATE)
        AppInitTable.scheduleIdle(this)
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
