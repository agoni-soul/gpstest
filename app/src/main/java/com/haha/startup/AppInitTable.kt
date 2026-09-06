package com.haha.startup

import android.app.Application
import com.haha.hahalearn.BuildConfig
import com.haha.startup.AppInitTable.ALL
import com.haha.startup.AppInitTable.run
import com.haha.startup.AppInitTable.scheduleIdle
import com.haha.startup.task.ActivityManagerInitTask
import com.haha.startup.task.LeakCanaryInitTask
import com.haha.startup.task.LogInitTask
import com.haha.startup.task.ReservedMmkvTask
import com.haha.startup.task.ReservedNetworkTask
import com.haha.startup.task.ReservedPushTask
import com.haha.startup.task.RouterInitTask
import com.haha.startup.task.ServiceLoaderInitTask
import com.haha.startup.task.TimeMonitorInitTask

/**
 * 启动任务表。Application 只按阶段调用 [run] / [scheduleIdle]。
 *
 * 预留：[ReservedMmkvTask]、[ReservedNetworkTask]、[ReservedPushTask]，实现后移入 [ALL]。
 */
object AppInitTable {
    private val ALL: List<AppInitTask> = listOf(
        LogInitTask,
        ActivityManagerInitTask,
        TimeMonitorInitTask,
        RouterInitTask,
        ServiceLoaderInitTask,
        LeakCanaryInitTask,
    )

    @Suppress("unused")
    private val RESERVED: List<AppInitTask> = listOf(
        ReservedMmkvTask,
        ReservedNetworkTask,
        ReservedPushTask,
    )

    fun of(stage: InitStage): List<AppInitTask> {
        return ALL.filter { it.stage == stage && (!it.debugOnly || BuildConfig.DEBUG) }
    }

    fun run(app: Application, stage: InitStage) {
        AppInitDispatcher.run(app, stage, of(stage))
    }

    fun scheduleIdle(app: Application) {
        AppInitDispatcher.scheduleIdle(app, of(InitStage.IDLE))
    }
}
