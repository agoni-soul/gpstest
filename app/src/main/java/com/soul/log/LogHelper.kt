package com.soul.log

import android.annotation.SuppressLint
import android.text.TextUtils
import com.soul.log.adapter.ILogAdapter
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
class LogHelper {

    companion object {
        val VERBOSE = 2
        val DEBUG = 3
        val INFO = 4
        val WARN = 5
        val ERROR = 6
        val ASSERT = 7

        private val sInstance by lazy(LazyThreadSafetyMode.NONE) {
            LogHelper()
        }

        fun getInstance(): LogHelper = sInstance
    }

    private val mList: MutableList<ILogAdapter> = ArrayList()

    private constructor()

    fun add(logAdapter: ILogAdapter) {
        mList.add(logAdapter)
    }

    fun clear() {
        mList.clear()
    }

    fun d(tag: String?, msg: String?) {
        log(DEBUG, tag, msg, null, null, null, null, null)
    }

    fun d(modelName: String?, tag: String?, msg: String?) {
        log(DEBUG, modelName, tag, msg, null, null, null, null, null)
    }

    fun v(tag: String?, msg: String?) {
        log(VERBOSE, tag, msg, null, null, null, null, null)
    }

    fun v(modelName: String?, tag: String?, msg: String?) {
        log(VERBOSE, modelName, tag, msg, null, null, null, null, null)
    }

    fun i(tag: String?, msg: String?) {
        log(INFO, tag, msg, null, null, null, null, null)
    }

    fun i(modelName: String?, tag: String?, msg: String?) {
        log(INFO, modelName, tag, msg, null, null, null, null, null)
    }

    fun w(tag: String?, msg: String?) {
        log(WARN, tag, msg, null, null, null, null, null)
    }

    fun w(modelName: String?, tag: String?, msg: String?) {
        log(WARN, modelName, tag, msg, null, null, null, null, null)
    }

    fun e(tag: String?, msg: String?) {
        log(ERROR, tag, msg, null, null, null, null, null)
    }

    fun e(modelName: String?, tag: String?, msg: String?) {
        log(ERROR, modelName, tag, msg, null, null, null, null, null)
    }

    fun a(tag: String?, msg: String?) {
        log(ASSERT, tag, msg, null, null, null, null, null)
    }

    fun a(modelName: String?, tag: String?, msg: String?) {
        log(ASSERT, modelName, tag, msg, null, null, null, null, null)
    }

    fun d(tag: String?, msg: Throwable?) {
        log(DEBUG, tag, null, msg, null, null, null, null)
    }

    fun d(modelName: String?, tag: String?, msg: Throwable?) {
        log(DEBUG, modelName, tag, null, msg, null, null, null, null)
    }

    fun v(tag: String?, msg: Throwable?) {
        log(VERBOSE, tag, null, msg, null, null, null, null)
    }

    fun v(modelName: String?, tag: String?, msg: Throwable?) {
        log(VERBOSE, modelName, tag, null, msg, null, null, null, null)
    }

    fun i(tag: String?, msg: Throwable?) {
        log(INFO, tag, null, msg, null, null, null, null)
    }

    fun i(modelName: String?, tag: String?, msg: Throwable?) {
        log(WARN, modelName, tag, null, msg, null, null, null, null)
    }

    fun w(tag: String?, msg: Throwable?) {
        log(WARN, tag, null, msg, null, null, null, null)
    }

    fun w(modelName: String?, tag: String?, msg: Throwable?) {
        log(WARN, modelName, tag, null, msg, null, null, null, null)
    }

    fun e(tag: String?, msg: Throwable?) {
        log(ERROR, tag, null, msg, null, null, null, null)
    }

    fun e(modelName: String?, tag: String?, msg: Throwable?) {
        log(ERROR, modelName, tag, null, msg, null, null, null, null)
    }

    fun a(tag: String?, msg: Throwable?) {
        log(ASSERT, tag, null, msg, null, null, null, null)
    }

    fun a(modelName: String?, tag: String?, msg: Throwable?) {
        log(ASSERT, modelName, tag, null, msg, null, null, null, null)
    }

    fun logJson(tag: String?, msg: JSONObject?) {
        log(DEBUG, tag, null, null, msg, null, null, null)
    }

    fun logJson(modelName: String?, tag: String?, msg: JSONObject?) {
        log(DEBUG, modelName, tag, null, null, msg, null, null, null)
    }

    fun logJson(tag: String?, msg: JSONArray?) {
        log(DEBUG, tag, null, null, null, msg, null, null)
    }

    fun logJson(modelName: String?, tag: String?, msg: JSONArray?) {
        log(DEBUG, modelName, tag, null, null, null, msg, null, null)
    }

    fun logMap(tag: String?, msg: Map<String?, Any?>?) {
        log(DEBUG, tag, null, null, null, null, msg, null)
    }

    fun logMap(modelName: String?, tag: String?, msg: Map<String?, Any?>?) {
        log(DEBUG, modelName, tag, null, null, null, null, msg, null)
    }

    fun logList(tag: String?, msg: List<*>?) {
        log(DEBUG, tag, null, null, null, null, null, msg)
    }

    fun logList(modelName: String?, tag: String?, msg: List<*>?) {
        log(DEBUG, modelName, tag, null, null, null, null, null, msg)
    }

    private fun log(
        logType: Int,
        tag: String?,
        msg: String?,
        throwable: Throwable?,
        jsonObject: JSONObject?,
        jsonArray: JSONArray?,
        map: Map<String?, Any?>?,
        list: List<*>?
    ) {
        log(logType, "", tag, msg, throwable, jsonObject, jsonArray, map, list)
    }

    private fun log(
        logType: Int,
        modelName: String?,
        tag: String?,
        msg: String?,
        throwable: Throwable?,
        jsonObject: JSONObject?,
        jsonArray: JSONArray?,
        map: Map<String?, Any?>?,
        list: List<*>?
    ) {
        for (i in mList.indices) {
            val adapter: ILogAdapter = mList[i]
            if (!adapter.filter(logType, tag)) continue
            if (!TextUtils.isEmpty(msg)) {
                adapter.log(logType, modelName, tag, msg)
            } else if (throwable != null) {
                adapter.log(logType, modelName, tag, throwable)
            } else if (jsonObject != null) {
                adapter.log(logType, modelName, tag, jsonObject)
            } else if (jsonArray != null) {
                adapter.log(logType, modelName, tag, jsonArray)
            } else if (map != null) {
                adapter.log(logType, modelName, tag, map)
            } else if (list != null) {
                adapter.log(logType, modelName, tag, list)
            }
        }
    }

    fun enable(enable: Boolean) {
        for (adapter in mList) {
            adapter.enable(enable)
        }
    }

    fun setLogLevel(logType: Int) {
        for (adapter in mList) {
            adapter.setLogLevel(logType)
        }
    }
}