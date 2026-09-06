package com.haha.startup.task

import android.app.Application
import com.haha.startup.AppInitTask
import com.haha.startup.InitStage
import com.haha.startup.task.ReservedPushTask.run

/**
 * 预留启动位。实现 [run] 后加入 [com.haha.startup.AppInitTable] 即可。
 */
object ReservedPushTask : AppInitTask {
    override val name = "push"
    override val stage = InitStage.IDLE
    override val dependsOn = listOf("log")

    override fun run(app: Application) = Unit
}
