package com.haha.main.logMonitor

import android.os.Looper
import android.util.Printer
import com.haha.log.DOFLogUtil
import com.haha.main.logMonitor.LogPrinterListener.Companion.UI_PERF_LEVEL_1
import com.haha.main.logMonitor.LogPrinterListener.Companion.UI_PERF_LEVEL_2
import com.haha.main.logMonitor.UiPerfMonitorConfig.Companion.TIME_WARNING_LEVEL_1
import com.haha.main.logMonitor.UiPerfMonitorConfig.Companion.TIME_WARNING_LEVEL_2

/**
 * @auther: haha
 * @Date:   2025/11/27
 * @Detail:
 */
class LogPrinter: Printer, UiPerfMonitorConfig {
    private val TAG = javaClass.simpleName
    private var mLogPrinter: LogPrinterListener? = null
    private var startTime = 0L

    constructor(listener: LogPrinterListener) {
        mLogPrinter = listener
    }

    /**
     * Write a line of text to the output.  There is no need to terminate
     * the given string with a newline.
     */
    override fun println(logInfo: String?) {
        if (startTime <= 0) {
            //发送消息，同时启动线程保存状态
            startTime = System.currentTimeMillis()
            mLogPrinter?.onStartLoop()
        } else {
            //执行消息，同时复位ANR线程状态
            val endTime = System.currentTimeMillis()

            DOFLogUtil.d(TAG, "dispatch handler time: ${endTime - startTime}")
            execuTime(logInfo, startTime, endTime)
            startTime = 0
        }
    }

    // 根据需要可以定义更多级别
    private fun execuTime(logInfo: String?, startTime: Long, endTime: Long) {
        var level = 0
        val time = endTime - startTime
        if (time > TIME_WARNING_LEVEL_2) {
            DOFLogUtil.d(TAG, "Warning_LEVEL_2:\r\nprintln: $logInfo")
            level = UI_PERF_LEVEL_2
        } else if (time > TIME_WARNING_LEVEL_1) {
            DOFLogUtil.d(TAG, "Warning_LEVEL_2:\r\nprintln: $logInfo")
            level = UI_PERF_LEVEL_1
        }
        mLogPrinter?.onEndLoop(startTime, endTime, logInfo, level)
    }

    fun startMonitor() {
        Looper.getMainLooper().setMessageLogging(this)
    }

    fun stopMonitor() {
        Looper.getMainLooper().setMessageLogging(null)
    }
}