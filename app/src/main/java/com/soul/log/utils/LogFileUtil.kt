package com.soul.log.utils

import android.text.TextUtils
import com.blankj.utilcode.util.SDCardUtils
import com.blankj.utilcode.util.Utils
import com.soul.log.strategy.AndroidLogFormat.Companion.getCurrentProcessName1

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2023/03/28
 *     desc   :
 *     version: 1.0
 * </pre>
 */
object LogFileUtil {
    const val LOG_DIR = "log"
    val FILE_SEP = System.getProperty("file.separator")
    const val PLUGIN_LOG_DIR = "plugin_log"

    /**
     * 获取 应用内部存储 日志文件夹路径
     */
    fun getLogFolder(): String {
        var logDir = LOG_DIR
        val processName = getProcessName()
        if (!TextUtils.isEmpty(processName)) {
            logDir = logDir + "_" + processName
        }
        return if (isSDCardAvailable()) {
            Utils.getApp().getExternalFilesDir(logDir).toString() + FILE_SEP
        } else {
            Utils.getApp().filesDir.toString() + FILE_SEP + logDir + FILE_SEP
        }
    }

    fun getPluginLogDir(): String {
        return if (isSDCardAvailable()) {
            Utils.getApp().getExternalFilesDir(PLUGIN_LOG_DIR).toString() + FILE_SEP
        } else {
            Utils.getApp().filesDir.toString() + FILE_SEP + PLUGIN_LOG_DIR + FILE_SEP
        }
    }

    private fun isSDCardAvailable(): Boolean {
        return SDCardUtils.isSDCardEnableByEnvironment() && Utils.getApp()
            .getExternalFilesDir(null) != null
    }

    fun getProcessName(): String {
        val currentProcess: String = getCurrentProcessName1() ?: return ""
        val names = currentProcess.split(":").toTypedArray()
        return if (names.size > 1) names[1] else ""
    }
}
