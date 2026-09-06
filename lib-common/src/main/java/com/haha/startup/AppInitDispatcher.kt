package com.haha.startup

import android.app.Application
import android.os.Looper
import com.haha.log.DOFLogUtil

/**
 * 按阶段执行任务表。ON_CREATE / ATTACH_BASE 主线程顺序跑；
 * IDLE 用 IdleHandler，一次空闲执行一条，避免堵首帧。
 */
object AppInitDispatcher {
    private const val TAG = "AppInit"

    fun run(app: Application, stage: InitStage, tasks: List<AppInitTask>) {
        if (stage == InitStage.IDLE) {
            scheduleIdle(app, tasks)
            return
        }
        tasks.forEach { execute(app, it) }
    }

    fun scheduleIdle(app: Application, tasks: List<AppInitTask>) {
        if (tasks.isEmpty()) {
            return
        }
        val queue = ArrayDeque(tasks)
        Looper.myQueue().addIdleHandler {
            val task = queue.removeFirstOrNull() ?: return@addIdleHandler false
            execute(app, task)
            queue.isNotEmpty()
        }
    }

    private fun execute(app: Application, task: AppInitTask) {
        val start = System.currentTimeMillis()
        task.run(app)
        DOFLogUtil.d(TAG, "${task.stage.name}/${task.name} ${System.currentTimeMillis() - start}ms")
    }
}
