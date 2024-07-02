package com.soul.log

import android.content.Context
import com.soul.log.adapter.*
import com.soul.log.utils.LogFileUtil
import com.soul.log.utils.SystemInfoUtil
import org.json.JSONArray
import org.json.JSONObject

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2023/03/28
 *     desc   :
 *     version: 1.0
 * </pre>
 */
object DOFLogUtil {

    private val TAG = javaClass.simpleName

    fun init() {
        init(true)
    }

    fun init(isDebug: Boolean) {
        LogHelper.getInstance().clear()
        if (isDebug) {
            LogHelper.getInstance().add(AndroidLogAdapter()) // 打印到Logcat
            LogHelper.getInstance().add(DiskLogAdapter(LogFileUtil.getLogFolder())) // 打印到文件
            LogHelper.getInstance().add(PluginDiskLogAdapter(LogFileUtil.getPluginLogDir())) // 插件日志
        }
        LogHelper.getInstance().add(CrashAdapter())
    }

    fun enable(enable: Boolean) {
        LogHelper.getInstance().enable(enable)
    }

    fun setLogLevel(logLevel: Int) {
        LogHelper.getInstance().setLogLevel(logLevel)
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

    fun v(tag: String?, vararg objects: Any) {
        val msg = getMsgs(objects)
        LogHelper.getInstance().v(tag, msg.toString())
    }

    fun vByModelName(modelName: String?, tag: String?, vararg objects: Any) {
        val msg = getMsgs(objects)
        LogHelper.getInstance().v(modelName, tag, msg.toString())
    }

    fun d(msg: String?) {
        LogHelper.getInstance().d(TAG, msg)
    }

//    fun d(tag: String?, msg: String?) {
//        LogHelper.getInstance().d(tag, msg)
//    }

    fun d(modelName: String?, tag: String?, msg: String?) {
        LogHelper.getInstance().d(modelName, tag, msg)
    }

    fun d(tag: String?, throwable: Throwable?) {
        LogHelper.getInstance().d(tag, throwable)
    }

    fun d(modelName: String?, tag: String?, throwable: Throwable?) {
        LogHelper.getInstance().d(modelName, tag, throwable)
    }

    fun d(tag: String?, vararg objects: Any) {
        val msg = getMsgs(objects)
        LogHelper.getInstance().d(tag, msg.toString())
    }

    fun dByModelName(modelName: String?, tag: String?, vararg objects: Any) {
        val msg = getMsgs(objects)
        LogHelper.getInstance().d(modelName, tag, msg.toString())
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

    fun i(tag: String?, vararg objects: Any) {
        val msg = getMsgs(objects)
        LogHelper.getInstance().i(tag, msg.toString())
    }

    fun iByModelName(modelName: String?, tag: String?, vararg objects: Any) {
        val msg = getMsgs(objects)
        LogHelper.getInstance().i(modelName, tag, msg.toString())
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

    fun w(tag: String?, vararg objects: Any) {
        val msg = getMsgs(objects)
        LogHelper.getInstance().w(tag, msg.toString())
    }

    fun wByName(modelName: String?, tag: String?, vararg objects: Any) {
        val msg = getMsgs(objects)
        LogHelper.getInstance().w(modelName, tag, msg.toString())
    }

    fun e(msg: String?) {
        LogHelper.getInstance().e(TAG, msg)
    }

//    fun e(tag: String?, msg: String?) {
//        LogHelper.getInstance().e(tag, msg)
//    }

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

    fun e(tag: String?, vararg objects: Any) {
        val msg = getMsgs(objects)
        LogHelper.getInstance().e(tag, msg.toString())
    }

    fun eByModelName(modelName: String?, tag: String?, vararg objects: Any) {
        val msg = getMsgs(objects)
        LogHelper.getInstance().e(modelName, tag, msg.toString())
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

    fun a(tag: String?, vararg objects: Any) {
        val msg = getMsgs(objects)
        LogHelper.getInstance().a(tag, msg.toString())
    }

    fun aByModelName(modelName: String?, tag: String?, vararg objects: Any) {
        val msg = getMsgs(objects)
        LogHelper.getInstance().a(modelName, tag, msg.toString())
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
     * 获取本地Log文件的路径
     */
    fun getLogDir(context: Context?): String? {
        return LogFileUtil.getLogFolder()
    }

    private fun getMsgs(objects: Array<out Any>): StringBuilder {
        val msg = StringBuilder()
        objects?.let {
            for (obj in objects) {
                msg.append(obj.toString()).append(" ")
            }
        }
        return msg
    }
}