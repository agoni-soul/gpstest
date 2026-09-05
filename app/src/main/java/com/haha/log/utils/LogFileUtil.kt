package com.haha.log.utils

import android.content.Context
import android.text.TextUtils
import com.blankj.utilcode.util.SDCardUtils
import com.blankj.utilcode.util.Utils
import com.haha.log.strategy.AndroidLogFormat.Companion.getCurrentProcessName1
import java.io.File

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2023/03/28
 *     desc   : 日志本地目录工具
 *     version: 1.1
 * </pre>
 */
object LogFileUtil {
    const val LOG_DIR = "log"
    val FILE_SEP: String = System.getProperty("file.separator") ?: File.separator
    const val PLUGIN_LOG_DIR = "plugin_log"

    @Volatile
    private var appContext: Context? = null

    fun init(context: Context?) {
        if (context != null) {
            appContext = context.applicationContext
        }
    }

    /**
     * 获取应用日志文件夹路径（优先外部专属目录，便于 adb pull）
     */
    fun getLogFolder(): String {
        var logDir = LOG_DIR
        val processName = getProcessName()
        if (!TextUtils.isEmpty(processName)) {
            logDir = logDir + "_" + processName
        }
        val folder = resolveDir(logDir)
        ensureDir(folder)
        return folder
    }

    fun getPluginLogDir(): String {
        val folder = resolveDir(PLUGIN_LOG_DIR)
        ensureDir(folder)
        return folder
    }

    fun getCrashLogDir(): String {
        val folder = getLogFolder() + "crash" + FILE_SEP
        ensureDir(folder)
        return folder
    }

    private fun resolveDir(dirName: String): String {
        val context = app() ?: return dirName + FILE_SEP
        val external = if (isSDCardAvailable(context)) {
            context.getExternalFilesDir(dirName)
        } else {
            null
        }
        val dir = external ?: File(context.filesDir, dirName)
        return dir.absolutePath + FILE_SEP
    }

    private fun isSDCardAvailable(context: Context): Boolean {
        return try {
            SDCardUtils.isSDCardEnableByEnvironment() && context.getExternalFilesDir(null) != null
        } catch (_: Exception) {
            false
        }
    }

    fun getProcessName(): String {
        val currentProcess: String = getCurrentProcessName1() ?: return ""
        val names = currentProcess.split(":").toTypedArray()
        return if (names.size > 1) names[1] else ""
    }

    fun ensureDir(path: String?): File? {
        if (path.isNullOrEmpty() || path == "null" || path == "null$FILE_SEP") {
            return null
        }
        return try {
            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            if (dir.exists() && dir.isDirectory) dir else null
        } catch (_: Exception) {
            null
        }
    }

    private fun app(): Context? {
        appContext?.let { return it }
        return try {
            Utils.getApp()
        } catch (_: Exception) {
            null
        }
    }
}
