package com.soul.log.adapter

import com.soul.log.LogHelper
import com.soul.log.strategy.AndroidLogFormat
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
open class DefaultLogAdapter : ILogAdapter {
    protected var mLogStrategy: ILogFormat? = AndroidLogFormat()
    protected var originLogLevel: Int = LogHelper.DEBUG
    protected var enable = true
    override fun log(logType: Int, modelName: String?, tag: String?, msg: String?) {}
    override fun log(logType: Int, modelName: String?, tag: String?, throwable: Throwable?) {}
    override fun log(logType: Int, modelName: String?, tag: String?, json: JSONObject?) {}
    override fun log(logType: Int, modelName: String?, tag: String?, jsonArray: JSONArray?) {}
    override fun log(logType: Int, modelName: String?, tag: String?, map: Map<String?, Any?>?) {}
    override fun log(logType: Int, modelName: String?, tag: String?, list: List<Any?>?) {}
    override fun setLogStrategy(logStrategy: ILogFormat?) {
        mLogStrategy = logStrategy
    }

    override fun enable(enable: Boolean) {
        this.enable = enable
    }

    override fun setLogLevel(logType: Int) {
        this.originLogLevel= logType
    }

    override fun filter(logType: Int, tag: String?): Boolean {
        return logType >= originLogLevel
    }
}