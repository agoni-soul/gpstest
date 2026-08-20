package com.haha.main.logMonitor

import android.os.Environment
import androidx.annotation.IntDef

/**
 * @auther: haha
 * @Date:   2025/11/27
 * @Detail:
 */
interface UiPerfMonitorConfig {
    companion object {
        // 定义卡顿报警域值
        val TIME_WARNING_LEVEL_1 = 100
        // 需要上报现场信息域值
        val TIME_WARNING_LEVEL_2 = 300

        val LOG_PATH = Environment.getExternalStorageDirectory().path + "androidtech/uiperf"

        //
        const val UI_PERF_LEVEL_0: Int = 0
        const val UI_PERF_LEVEL_1: Int = 1
        const val UI_PERF_LEVEL_2: Int = 2

        //
        @IntDef(UI_PERF_LEVEL_0, UI_PERF_LEVEL_1, UI_PERF_LEVEL_2)
        @Retention(AnnotationRetention.SOURCE)
        annotation class PER_LEVEL

        const val UI_PERF_MONITOR_START: Int = 0x01
        const val UI_PERF_MONITOR_STOP: Int = 0x01 shl 1

        const val FILENAME: String = "UiMonitorLog"
    }
}