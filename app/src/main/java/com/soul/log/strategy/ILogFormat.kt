package com.soul.log.strategy

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

open interface ILogFormat {
    fun format(msg: String?): String?
    fun format(msg: Throwable?): String?
    fun format(json: JSONObject?): String?
    fun format(jsonArray: JSONArray?): String?
    fun format(map: Map<*, *>?): String?
    fun format(list: List<*>?): String?
    fun format(modelName: String?, tag: String?, msg: String?): String?
}
