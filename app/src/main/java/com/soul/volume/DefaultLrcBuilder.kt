package com.soul.volume

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.StringReader
import java.lang.*
import java.util.Collections


/**
 *     author : yangzy33
 *     time   : 2024-06-24
 *     desc   :
 *     version: 1.0
 */
class DefaultLrcBuilder: ILrcBuilder {
    val TAG: String = javaClass.simpleName

    override fun getLrcRows(rawLrc: String?): MutableList<LrcRow>? {
        Log.d(TAG, "getLrcRows by rawString")
        if (rawLrc.isNullOrBlank()) {
            Log.d(TAG, "getLrcRows rawLrc null or blank")
            return null
        }
        val reader = StringReader(rawLrc)
        val br = BufferedReader(reader)
        var line: String?
        val rows = mutableListOf<LrcRow>()
        try {
            do {
                line = br.readLine()
                if (line.isNullOrEmpty()) {
                    continue
                } else {
                    val lrcRows = LrcRow.createRows(line)
                    if (!lrcRows.isNullOrEmpty()) {
                        for (row in lrcRows) {
                            rows.add(row)
                        }
                    }
                }
            } while (line != null)
            if (rows.isNotEmpty()) {
                rows.sort()
            }
            return rows
        } catch (e: Exception) {
            Log.e(TAG, "parse exception = ${e.message}")
            return null
        } finally {
            try {
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            reader.close()
        }
    }
}