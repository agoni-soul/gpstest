package com.haha.servicerouterutils.utils

import android.util.Log

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */

object Logger {

    private val PREFIX = "[DOFRouter]::"
    private var isDebug = false

    fun openDebug() {
        isDebug = true
    }

    fun d(msg: String, throwable: Throwable? = null) {
        if (isDebug) {
            Log.d(PREFIX, msg, throwable)
        }
    }

    fun i(msg: String, throwable: Throwable? = null) {
        if (isDebug) {
            Log.i(PREFIX, msg, throwable)
        }
    }

    fun w(msg: String, throwable: Throwable? = null) {
        Log.w(PREFIX, msg, throwable)
    }

    fun wtf(msg: String, throwable: Throwable? = null) {
        Log.wtf(PREFIX, msg, throwable)
    }

    fun e(msg: String, throwable: Throwable? = null) {
        Log.e(PREFIX, msg, throwable)
    }
}