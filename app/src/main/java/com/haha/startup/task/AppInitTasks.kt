package com.haha.startup.task

import android.app.Application
import com.haha.base.ActivityManager
import com.haha.hahalearn.BuildConfig
import com.haha.leakcanary.LeakCanaryInstaller
import com.haha.log.DOFLogUtil
import com.haha.main.timeMonitor.TimeMonitorConfig
import com.haha.main.timeMonitor.TimeMonitorManager
import com.haha.service.impl.service.ServiceLoader
import com.haha.servicerouter.core.DOFRouter
import com.haha.startup.AppInitTask
import com.haha.startup.InitStage

object LogInitTask : AppInitTask {
    override val name = "log"
    override val stage = InitStage.ATTACH_BASE

    override fun run(app: Application) {
        DOFLogUtil.init(app)
    }
}

object ActivityManagerInitTask : AppInitTask {
    override val name = "activity-manager"
    override val stage = InitStage.ATTACH_BASE
    override val dependsOn = listOf("log")

    override fun run(app: Application) {
        ActivityManager.init(app)
    }
}

object TimeMonitorInitTask : AppInitTask {
    override val name = "time-monitor"
    override val stage = InitStage.ATTACH_BASE
    override val dependsOn = listOf("log")

    override fun run(app: Application) {
        TimeMonitorManager.getInstance()
            .resetTimeMonitor(TimeMonitorConfig.TIME_MONITOR_ID_APPLICATION_START)
        TimeMonitorManager.getInstance()
            .getTimeMonitor(TimeMonitorConfig.TIME_MONITOR_ID_APPLICATION_START)
            .startMonitor()
    }
}

object RouterInitTask : AppInitTask {
    override val name = "router"
    override val stage = InitStage.ON_CREATE
    override val dependsOn = listOf("log")

    override fun run(app: Application) {
        DOFRouter.openDebug()
        DOFRouter.init(app)
    }
}

object ServiceLoaderInitTask : AppInitTask {
    override val name = "service-loader"
    override val stage = InitStage.ON_CREATE
    override val dependsOn = listOf("log")

    override fun run(app: Application) {
        ServiceLoader.init(app, BuildConfig.DEBUG)
    }
}

object LeakCanaryInitTask : AppInitTask {
    override val name = "leakcanary"
    override val stage = InitStage.IDLE

    override fun run(app: Application) {
        LeakCanaryInstaller.install(app)
    }
}
