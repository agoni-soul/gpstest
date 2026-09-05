package com.haha.log.adapter

import android.util.Log
import com.blankj.utilcode.util.CrashUtils
import com.blankj.utilcode.util.FileIOUtils
import com.haha.log.strategy.AndroidLogFormat
import com.haha.log.utils.LogFileUtil
import java.io.File

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2023/03/28
 *     desc   : 未捕获异常写入本地 crash 目录，并同步追加到小时日志
 *     version: 1.1
 * </pre>
 */
class CrashAdapter : DefaultLogAdapter() {

    init {
        install()
    }

    private fun install() {
        val crashDirPath = LogFileUtil.getCrashLogDir()
        val crashDir = File(crashDirPath)
        try {
            CrashUtils.init(crashDir)
        } catch (_: Throwable) {
        }
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            writeCrashSync(thread, throwable)
            try {
                previous?.uncaughtException(thread, throwable)
            } catch (_: Throwable) {
            }
        }
    }

    private fun writeCrashSync(thread: Thread, throwable: Throwable) {
        try {
            val formatter = AndroidLogFormat()
            val content = buildString {
                append("UncaughtException in thread: ").append(thread.name).append('\n')
                append(formatter.format(throwable))
            }
            val crashFile = File(
                LogFileUtil.getCrashLogDir(),
                "crash_" + System.currentTimeMillis() + ".txt"
            )
            FileIOUtils.writeFileFromString(crashFile.absolutePath, content, true)
            val hourlyFile = File(
                LogFileUtil.getLogFolder(),
                "log_" + java.text.SimpleDateFormat(
                    "yyyyMMddHH",
                    java.util.Locale.ENGLISH
                ).format(java.util.Date()) + ".txt"
            )
            FileIOUtils.writeFileFromString(hourlyFile.absolutePath, content, true)
        } catch (e: Throwable) {
            Log.e("CrashAdapter", "write crash log failed", e)
        }
    }
}
