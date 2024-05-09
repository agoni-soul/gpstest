package com.soul.log.adapter

import android.util.Log
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
class AndroidLogAdapter : DefaultLogAdapter() {
    override fun filter(logType: Int, tag: String?): Boolean {
        return enable && super.filter(logType, tag)
    }

    override fun log(logType: Int, modelName: String?, tag: String?, msg: String?) {
        Log.println(logType, tag, mLogStrategy!!.format(modelName, tag, msg)!!)
    }

    override fun log(logType: Int, modelName: String?, tag: String?, throwable: Throwable?) {
        log(logType, modelName, tag, mLogStrategy!!.format(throwable))
    }

    override fun log(logType: Int, modelName: String?, tag: String?, json: JSONObject?) {
        log(logType, modelName, tag, mLogStrategy!!.format(json))
    }

    override fun log(logType: Int, modelName: String?, tag: String?, jsonArray: JSONArray?) {
        log(logType, modelName, tag, mLogStrategy!!.format(jsonArray))
    }

    override fun log(logType: Int, modelName: String?, tag: String?, map: Map<String?, Any?>?) {
        log(logType, modelName, tag, mLogStrategy!!.format(map))
    }

    override fun log(logType: Int, modelName: String?, tag: String?, list: List<*>?) {
        log(logType, modelName, tag, mLogStrategy!!.format(list))
    }
}
