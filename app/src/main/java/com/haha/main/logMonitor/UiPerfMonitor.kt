package com.haha.main.logMonitor

import android.os.Looper
import android.util.Log
import com.haha.main.logMonitor.UiPerfMonitorConfig.Companion.LOG_PATH
import com.haha.main.logMonitor.UiPerfMonitorConfig.Companion.UI_PERF_LEVEL_1
import com.haha.main.logMonitor.UiPerfMonitorConfig.Companion.UI_PERF_LEVEL_2
import com.haha.main.logMonitor.UiPerfMonitorConfig.Companion.UI_PERF_MONITOR_START
import com.haha.main.logMonitor.UiPerfMonitorConfig.Companion.UI_PERF_MONITOR_STOP
import java.io.File

/**
 * @auther: haha
 * @Date:   2025/12/1
 * @Detail:
 */
class UiPerfMonitor: UiPerfMonitorConfig, LogPrinterListener {
    private val TAG = javaClass.simpleName

    companion object {
        private var mInstance: UiPerfMonitor? = null
        private var mLogPrinter: LogPrinter? = null
        private var mLogWriteThread: LogWriteThread? = null
        private var monitorState = UI_PERF_MONITOR_STOP
        private var mCpuInfoSampler: CpuInfoSampler? = null

        fun getInstance(): UiPerfMonitor {
            if (mInstance == null) {
                synchronized(UiPerfMonitor::class.java) {
                    if (mInstance == null) {
                        mInstance = UiPerfMonitor()
                    }
                }
            }
            return mInstance!!
        }
    }

    private constructor() {
        mCpuInfoSampler = CpuInfoSampler()
        mLogPrinter = LogPrinter(this)
        mLogWriteThread = LogWriteThread()
        initLogPath()
    }

    fun startMonitor() {
        mLogPrinter?.startMonitor()
        monitorState = UI_PERF_MONITOR_START
    }

    fun stopMonitor() {
        mLogPrinter?.stopMonitor()
        mCpuInfoSampler?.stop()
        monitorState = UI_PERF_MONITOR_START
    }

    fun isMonitoring(): Boolean = monitorState == UI_PERF_MONITOR_START

    private fun initLogPath() {
        val logPath = File(LOG_PATH)
        if (!logPath.exists()) {
            val mkdir = logPath.mkdir()
            Log.d(TAG, "mkdir: ${mkdir}: $LOG_PATH")
        }
    }

    override fun onStartLoop() {
        mCpuInfoSampler?.start()
    }

    override fun onEndLoop(startTime: Long, endTime: Long, logInfo: String?, @UiPerfMonitorConfig.Companion.PER_LEVEL level: Int) {
        mCpuInfoSampler?.stop()
        when (level) {
            UI_PERF_LEVEL_1 -> {
                val size = mCpuInfoSampler?.getStatCpuInfo()?.size ?: return
                Log.d(TAG, "onEndLoop TIME_WARNING_LEVEL_! & cupsize: $size")
                if (size > 0) {
                    val sb = StringBuilder("startTime: ")
                    sb.append(startTime)
                    sb.append(" endTime: ")
                    sb.append(endTime)
                    sb.append(" handleTime: ")
                    sb.append(endTime - startTime)
                    for (info in mCpuInfoSampler!!.getStatCpuInfo()) {
                        sb.append("\r\n")
                        sb.append(info.toString())
                    }
                    mLogWriteThread?.saveLog(sb.toString())
                }
            }
            UI_PERF_LEVEL_2 -> {

            }
            else -> {

            }
        }
    }
}