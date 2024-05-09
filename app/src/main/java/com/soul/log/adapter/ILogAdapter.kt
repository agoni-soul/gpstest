package com.soul.log.adapter

import com.soul.log.strategy.ILogFormat
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
open interface ILogAdapter {
    fun log(logType: Int, modelName: String?, tag: String?, msg: String?)
    fun log(logType: Int, modelName: String?, tag: String?, throwable: Throwable?)
    fun log(logType: Int, modelName: String?, tag: String?, json: JSONObject?)
    fun log(logType: Int, modelName: String?, tag: String?, jsonArray: JSONArray?)
    fun log(logType: Int, modelName: String?, tag: String?, map: Map<String?, Any?>?)
    fun log(logType: Int, modelName: String?, tag: String?, list: List<Any?>?)

    /**
     * 设置输出格式
     */
    fun setLogStrategy(logStrategy: ILogFormat?)

    // 开关
    fun enable(loggable: Boolean)

    // 日志等级
    fun setLogLevel(logType: Int)

    // 过滤日志，level or tag
    fun filter(logType: Int, tag: String?): Boolean
}