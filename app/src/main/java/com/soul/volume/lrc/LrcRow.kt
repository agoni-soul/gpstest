package com.soul.volume.lrc

import kotlin.math.pow


/**
 *     author : yangzy33
 *     time   : 2024-06-21
 *     desc   :
 *     version: 1.0
 */
class LrcRow() : Comparable<LrcRow> {
    private var strTime: String? = null
    var time: Long = 0
        private set
    var content: String? = null
        private set

    constructor(strTime: String, time: Long, content: String) : this() {
        this.strTime = strTime
        this.time = time
        this.content = content
    }

    companion object {
        fun createRows(standardLrcLine: String): MutableList<LrcRow>? {
            try {
                if (standardLrcLine.indexOf("[") != 0) {
                    return null
                }
                val lastIndexOfRightBracket = standardLrcLine.lastIndexOf("]")
                val content = standardLrcLine.substring(lastIndexOfRightBracket + 1)
                val times =
                    standardLrcLine.substring(0, lastIndexOfRightBracket + 1)
                        .replace("[", "-")
                        .replace("]", "-")
                val arrTimes = times.split("-")
                val listTimes = mutableListOf<LrcRow>()
                for (temp in arrTimes) {
                    if (temp.trim().isEmpty()) {
                        continue
                    }
                    val lrcRow = LrcRow(temp, timeConvert(temp), content)
                    listTimes.add(lrcRow)
                }
                return listTimes
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        private fun timeConvert(timeString: String): Long {
            val timeTemp = timeString.replace(".", ":")
            val times = timeTemp.split(":")
            val minute = times[0].toIntOrNull() ?: 0
            val second: Int = if (times[1].length > 2) {
                times[1].substring(0, 2).toIntOrNull() ?: 0
            } else {
                (times[1].toIntOrNull() ?: 0) % 60
            }
            val milli: Int = if (times[2].length > 3) {
                times[2].substring(0, 3).toIntOrNull() ?: 0
            } else {
                times[2].toIntOrNull() ?: 0
            }
            return minute * 60 * 1000L + second * 1000L + milli
        }
    }

    override fun toString(): String {
        return "[$strTime] $content"
    }

    override fun compareTo(other: LrcRow): Int {
        var differ = this.time - other.time
        val boundary = 2.0.pow(32.0).toLong()
        if (differ >= boundary || differ < boundary * (-1)) {
            differ %= boundary
        }
        return differ.toInt()
    }
}