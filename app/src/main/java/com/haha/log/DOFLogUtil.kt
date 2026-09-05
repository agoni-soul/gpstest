package com.haha.log

import android.content.Context
import com.haha.log.adapter.AndroidLogAdapter
import com.haha.log.adapter.CrashAdapter
import com.haha.log.adapter.DiskLogAdapter
import com.haha.log.adapter.PluginDiskLogAdapter
import com.haha.log.utils.LogFileUtil
import org.json.JSONArray
import org.json.JSONObject

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2023/03/28
 *     desc   : 统一日志入口：Logcat 打印 + 本地文件存储
 *     version: 1.1
 * </pre>
 */
object DOFLogUtil {

    private val TAG = javaClass.simpleName

    @Volatile
    private var initialized = false

    @JvmOverloads
    fun init(context: Context? = null, isDebug: Boolean = true) {
        if (context != null) {
            LogFileUtil.init(context)
        }
        val helper = LogHelper.getInstance()
        helper.clear()
        if (isDebug) {
            helper.add(AndroidLogAdapter())
        }
        // 无论 debug / release 都落盘，保证本地可回溯
        helper.add(DiskLogAdapter(LogFileUtil.getLogFolder()))
        helper.add(PluginDiskLogAdapter(LogFileUtil.getPluginLogDir()))
        helper.add(CrashAdapter())
        initialized = true
        d(TAG, "DOFLogUtil initialized, debug=$isDebug, logDir=${LogFileUtil.getLogFolder()}")
    }

    fun init(isDebug: Boolean) {
        init(null, isDebug)
    }

    fun enable(enable: Boolean) {
        LogHelper.getInstance().enable(enable)
    }

    fun setLogLevel(logLevel: Int) {
        LogHelper.getInstance().setLogLevel(logLevel)
    }

    fun flush(timeoutMs: Long = 1000L) {
        LogHelper.getInstance().flush(timeoutMs)
    }

    fun log(level: Int, tag: String?, msg: String?) {
        LogHelper.getInstance().log(level, tag, msg, null)
    }

    fun log(level: Int, tag: String?, msg: String?, throwable: Throwable?) {
        LogHelper.getInstance().log(level, tag, msg, throwable)
    }

    fun v(msg: String?) {
        LogHelper.getInstance().v(TAG, msg)
    }

    fun v(tag: String?, msg: String?) {
        LogHelper.getInstance().v(tag, msg)
    }

    fun v(modelName: String?, tag: String?, msg: String?) {
        LogHelper.getInstance().v(modelName, tag, msg)
    }

    fun v(tag: String?, throwable: Throwable?) {
        LogHelper.getInstance().v(tag, throwable)
    }

    fun v(modelName: String?, tag: String?, throwable: Throwable?) {
        LogHelper.getInstance().v(modelName, tag, throwable)
    }

    fun v(tag: String?, vararg objects: Any?) {
        LogHelper.getInstance().v(tag, getMsgs(objects))
    }

    fun vByModelName(modelName: String?, tag: String?, vararg objects: Any?) {
        LogHelper.getInstance().v(modelName, tag, getMsgs(objects))
    }

    fun d(msg: String?) {
        LogHelper.getInstance().d(TAG, msg)
    }

    fun d(tag: String?, msg: String?) {
        LogHelper.getInstance().d(tag, msg)
    }

    fun d(modelName: String?, tag: String?, msg: String?) {
        LogHelper.getInstance().d(modelName, tag, msg)
    }

    fun d(tag: String?, throwable: Throwable?) {
        LogHelper.getInstance().d(tag, throwable)
    }

    fun d(modelName: String?, tag: String?, throwable: Throwable?) {
        LogHelper.getInstance().d(modelName, tag, throwable)
    }

    fun d(tag: String?, vararg objects: Any?) {
        LogHelper.getInstance().d(tag, getMsgs(objects))
    }

    fun dByModelName(modelName: String?, tag: String?, vararg objects: Any?) {
        LogHelper.getInstance().d(modelName, tag, getMsgs(objects))
    }

    fun i(msg: String?) {
        LogHelper.getInstance().i(TAG, msg)
    }

    fun i(tag: String?, msg: String?) {
        LogHelper.getInstance().i(tag, msg)
    }

    fun i(modelName: String?, tag: String?, msg: String?) {
        LogHelper.getInstance().i(modelName, tag, msg)
    }

    fun i(tag: String?, throwable: Throwable?) {
        LogHelper.getInstance().i(tag, throwable)
    }

    fun i(modelName: String?, tag: String?, throwable: Throwable?) {
        LogHelper.getInstance().i(modelName, tag, throwable)
    }

    fun i(tag: String?, vararg objects: Any?) {
        LogHelper.getInstance().i(tag, getMsgs(objects))
    }

    fun iByModelName(modelName: String?, tag: String?, vararg objects: Any?) {
        LogHelper.getInstance().i(modelName, tag, getMsgs(objects))
    }

    fun w(msg: String?) {
        LogHelper.getInstance().w(TAG, msg)
    }

    fun w(tag: String?, msg: String?) {
        LogHelper.getInstance().w(tag, msg)
    }

    fun w(modelName: String?, tag: String?, msg: String?) {
        LogHelper.getInstance().w(modelName, tag, msg)
    }

    fun w(tag: String?, throwable: Throwable?) {
        LogHelper.getInstance().w(tag, throwable)
    }

    fun w(modelName: String?, tag: String?, throwable: Throwable?) {
        LogHelper.getInstance().w(modelName, tag, throwable)
    }

    fun w(tag: String?, vararg objects: Any?) {
        LogHelper.getInstance().w(tag, getMsgs(objects))
    }

    fun wByName(modelName: String?, tag: String?, vararg objects: Any?) {
        LogHelper.getInstance().w(modelName, tag, getMsgs(objects))
    }

    fun e(msg: String?) {
        LogHelper.getInstance().e(TAG, msg)
    }

    fun e(tag: String?, msg: String?) {
        LogHelper.getInstance().e(tag, msg)
    }

    fun e(modelName: String?, tag: String?, msg: String?) {
        LogHelper.getInstance().e(modelName, tag, msg)
    }

    fun e(tag: String?, throwable: Throwable?) {
        LogHelper.getInstance().e(tag, throwable)
    }

    fun e(throwable: Throwable?) {
        LogHelper.getInstance().e(TAG, throwable)
    }

    fun e(modelName: String?, tag: String?, throwable: Throwable?) {
        LogHelper.getInstance().e(modelName, tag, throwable)
    }

    fun e(tag: String?, vararg objects: Any?) {
        LogHelper.getInstance().e(tag, getMsgs(objects))
    }

    fun eByModelName(modelName: String?, tag: String?, vararg objects: Any?) {
        LogHelper.getInstance().e(modelName, tag, getMsgs(objects))
    }

    fun a(msg: String?) {
        LogHelper.getInstance().a(TAG, msg)
    }

    fun a(tag: String?, msg: String?) {
        LogHelper.getInstance().a(tag, msg)
    }

    fun a(modelName: String?, tag: String?, msg: String?) {
        LogHelper.getInstance().a(modelName, tag, msg)
    }

    fun a(tag: String?, throwable: Throwable?) {
        LogHelper.getInstance().a(tag, throwable)
    }

    fun a(modelName: String?, tag: String?, throwable: Throwable?) {
        LogHelper.getInstance().a(modelName, tag, throwable)
    }

    fun a(tag: String?, vararg objects: Any?) {
        LogHelper.getInstance().a(tag, getMsgs(objects))
    }

    fun aByModelName(modelName: String?, tag: String?, vararg objects: Any?) {
        LogHelper.getInstance().a(modelName, tag, getMsgs(objects))
    }

    fun logJson(tag: String?, jsonObject: JSONObject?) {
        LogHelper.getInstance().logJson(tag, jsonObject)
    }

    fun logJson(modelName: String?, tag: String?, jsonObject: JSONObject?) {
        LogHelper.getInstance().logJson(modelName, tag, jsonObject)
    }

    fun logJson(tag: String?, jsonArray: JSONArray?) {
        LogHelper.getInstance().logJson(tag, jsonArray)
    }

    fun logJson(modelName: String?, tag: String?, jsonArray: JSONArray?) {
        LogHelper.getInstance().logJson(modelName, tag, jsonArray)
    }

    fun logJson(tag: String?, json: String?) {
        if (json.isNullOrBlank()) {
            d(tag, "json is empty")
            return
        }
        val trimmed = json.trim()
        try {
            if (trimmed.startsWith("[")) {
                logJson(tag, JSONArray(trimmed))
            } else {
                logJson(tag, JSONObject(trimmed))
            }
        } catch (ex: Exception) {
            e(tag, "invalid json: $json, error=${ex.message}")
        }
    }

    fun logMap(tag: String?, map: Map<String?, Any?>?) {
        LogHelper.getInstance().logMap(tag, map)
    }

    fun logMap(modelName: String?, tag: String?, map: Map<String?, Any?>?) {
        LogHelper.getInstance().logMap(modelName, tag, map)
    }

    fun logList(tag: String?, list: List<*>?) {
        LogHelper.getInstance().logList(tag, list)
    }

    fun logList(modelName: String?, tag: String?, list: List<*>?) {
        LogHelper.getInstance().logList(modelName, tag, list)
    }

    /**
     * 获取本地 Log 文件目录
     */
    fun getLogDir(context: Context? = null): String {
        if (context != null) {
            LogFileUtil.init(context)
        }
        return LogFileUtil.getLogFolder()
    }

    fun getCrashLogDir(): String {
        return LogFileUtil.getCrashLogDir()
    }

    fun isInitialized(): Boolean = initialized

    private fun getMsgs(objects: Array<out Any?>): String {
        if (objects.isEmpty()) {
            return "null"
        }
        return objects.joinToString(" ") { it?.toString() ?: "null" }
    }
}
