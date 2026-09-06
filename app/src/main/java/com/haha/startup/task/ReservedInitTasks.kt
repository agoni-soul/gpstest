package com.haha.startup.task

import android.app.Application
import com.haha.startup.AppInitTask
import com.haha.startup.InitStage
import com.haha.startup.task.ReservedMmkvTask.run

/**
 * 预留启动位。实现 [run] 后加入 [com.haha.startup.AppInitTable] 即可，不必改 Application。
 */
object ReservedMmkvTask : AppInitTask {
    override val name = "mmkv"
    override val stage = InitStage.ON_CREATE
    override val dependsOn = listOf("log")

    override fun run(app: Application) = Unit
}

object ReservedNetworkTask : AppInitTask {
    override val name = "network"
    override val stage = InitStage.ON_CREATE
    override val dependsOn = listOf("log")

    override fun run(app: Application) = Unit
}

object ReservedPushTask : AppInitTask {
    override val name = "push"
    override val stage = InitStage.IDLE
    override val dependsOn = listOf("log")

    override fun run(app: Application) = Unit
}
