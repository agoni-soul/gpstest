package com.haha.service.impl.core

import android.util.Log

/**
 * 默认打到 Logcat。由 [com.haha.service.impl.service.ServiceLoader.init] 在未自定义 Logger 时安装。
 */
class LogcatLogger : Debugger.Logger {
    override fun d(msg: String?, vararg args: Any?) {
        Log.d(Debugger.LOG_TAG, format(msg, args))
    }

    override fun i(msg: String?, vararg args: Any?) {
        Log.i(Debugger.LOG_TAG, format(msg, args))
    }

    override fun w(msg: String?, vararg args: Any?) {
        Log.w(Debugger.LOG_TAG, format(msg, args))
    }

    override fun w(t: Throwable?) {
        Log.w(Debugger.LOG_TAG, t)
    }

    override fun e(msg: String?, vararg args: Any?) {
        Log.e(Debugger.LOG_TAG, format(msg, args))
    }

    override fun e(t: Throwable?) {
        Log.e(Debugger.LOG_TAG, "", t)
    }

    override fun fatal(msg: String?, vararg args: Any?) {
        Log.e(Debugger.LOG_TAG, format(msg, args))
    }

    override fun fatal(t: Throwable?) {
        Log.e(Debugger.LOG_TAG, "", t)
    }

    private fun format(msg: String?, args: Array<out Any?>): String {
        if (msg.isNullOrEmpty()) {
            return args.contentToString()
        }
        return if (args.isEmpty()) {
            msg
        } else {
            try {
                String.format(msg, *args)
            } catch (_: Exception) {
                "$msg ${args.contentToString()}"
            }
        }
    }
}
