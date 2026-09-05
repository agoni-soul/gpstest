package com.haha.service.impl.core

/**
 *
 * @author : haha
 * @date   : 2024-09-19
 * @desc   : 打印日志
 * @version: 1.0
 *
 */
object Debugger {
    /**
     * 输出调试信息的Tag
     */
    val LOG_TAG: String = "Base_INFO"

    private var sLogger: Logger? = null

    private var sEnableDebug = false

    private var sEnableLog = false

    /**
     * 设置Logger
     */
    fun setLogger(logger: Logger?) {
        sLogger = logger
    }

    fun isLogSetting(): Boolean {
        return sLogger != null
    }

    /**
     * 调试模式开关。调试模式开启后，可以在发生错误时抛出异常，及时暴漏问题。建议测试环境开启，线上环境应该关闭。
     */
    fun setEnableDebug(enableDebug: Boolean) {
        sEnableDebug = enableDebug
    }

    fun isEnableDebug(): Boolean {
        return sEnableDebug
    }

    /**
     * Log开关。建议测试环境开启，线上环境应该关闭。
     */
    fun setEnableLog(enableLog: Boolean) {
        sEnableLog = enableLog
    }

    fun isEnableLog(): Boolean {
        return sEnableLog
    }

    fun d(msg: String?, vararg args: Any?) {
        if (sLogger != null) {
            sLogger!!.d(msg, *args)
        }
    }

    fun i(msg: String?, vararg args: Any?) {
        if (sLogger != null) {
            sLogger!!.i(msg, *args)
        }
    }

    fun w(msg: String?, vararg args: Any?) {
        if (sLogger != null) {
            sLogger!!.w(msg, *args)
        }
    }

    fun w(t: Throwable?) {
        if (sLogger != null) {
            sLogger!!.w(t)
        }
    }

    fun e(msg: String?, vararg args: Any?) {
        if (sLogger != null) {
            sLogger!!.e(msg, *args)
        }
    }

    fun e(t: Throwable?) {
        if (sLogger != null) {
            sLogger!!.e(t)
        }
    }

    fun fatal(msg: String?, vararg args: Any?) {
        if (sLogger != null) {
            sLogger!!.fatal(msg, *args)
        }
    }

    fun fatal(t: Throwable?) {
        if (sLogger != null) {
            sLogger!!.fatal(t)
        }
    }

    interface Logger {
        fun d(msg: String?, vararg args: Any?)

        fun i(msg: String?, vararg args: Any?)

        fun w(msg: String?, vararg args: Any?)

        fun w(t: Throwable?)

        fun e(msg: String?, vararg args: Any?)

        fun e(t: Throwable?)

        fun fatal(msg: String?, vararg args: Any?)

        fun fatal(t: Throwable?)
    }
}