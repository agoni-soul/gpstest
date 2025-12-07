package com.soul.main.logMonitor

/**
 * @auther: soulagoni
 * @Date:   2025/11/27
 * @Detail:
 */
interface LogPrinterListener {
    companion object {

        // 定义卡顿报警域值
        val UI_PERF_LEVEL_1 = 1000
        // 需要上报现场信息域值
        val UI_PERF_LEVEL_2 = 3000
    }

    fun onStartLoop()

    fun onEndLoop(startTime: Long, endTime: Long, logInfo: String?, level: Int)
}