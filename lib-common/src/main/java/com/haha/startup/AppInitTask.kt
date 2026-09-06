package com.haha.startup

import android.app.Application

/**
 * 启动任务。第一版按任务表顺序、主线程执行；
 * [dependsOn] / [mainThread] / [needWait] 预留给后续 DAG / 线程池。
 */
interface AppInitTask {
    val name: String
    val stage: InitStage

    val dependsOn: List<String>
        get() = emptyList()

    val mainThread: Boolean
        get() = true

    val needWait: Boolean
        get() = stage != InitStage.IDLE

    val debugOnly: Boolean
        get() = false

    fun run(app: Application)
}
