package com.soul.waterfall

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader


/**
 *     author : yangzy33
 *     time   : 2024-02-22
 *     desc   :
 *     version: 1.0
 */
object ReadJsonUtil {
    fun readJson(fileName: String, context: Context): String? {
        return try {
            val inputStreamReader = InputStreamReader(context.assets.open(fileName), "UTF-8")
            val br = BufferedReader(inputStreamReader)
            var line: String? = ""
            val builder = StringBuffer()
            while (line != null) {
                builder.append(line)
                line = br.readLine()
            }
            builder.toString()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }
}