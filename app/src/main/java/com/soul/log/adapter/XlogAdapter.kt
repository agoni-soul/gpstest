package com.soul.log.adapter

import android.text.TextUtils
import android.util.Log
import com.soul.log.LogHelper
import com.soul.log.utils.LogFileUtil
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
//class XlogAdapter(filePath: String?) : DefaultLogAdapter() {
//    private var xlog: Xlog? = null
//
//    init {
//        try {
//            //新版本不再采用stl
//            System.loadLibrary("c++_shared")
//            System.loadLibrary("marsxlog")
//        } catch (ignored: Throwable) {
//        }
//
//        // 保证每个进程独享一个日志文件。而且保存log的目录请使用单独的目录，不要存放任何其他文件防止被xlog自动清理功能误删。
//        val processName: String = LogFileUtil.getProcessName()
//        var cachePath: String = Utils.getApp().getCacheDir() + "/xlog"
//        var namePrefix = "xlog"
//        if (!TextUtils.isEmpty(processName)) {
//            cachePath = cachePath + "_" + processName
//            namePrefix = namePrefix + "_" + processName
//        }
//
//        // this is necessary, or may crash for SIGBUS
//        Companion.cachePath = cachePath
//        Companion.filePath = filePath
//        xlog = Xlog()
//        val logInstancePtr: Long = 0
//        xlog.setConsoleLogOpen(logInstancePtr, false)
//        //打开
//        xlog.open(
//            false,
//            Xlog.LEVEL_DEBUG,
//            Xlog.AppednerModeAsync,
//            cachePath,
//            filePath,
//            namePrefix,
//            "d69f1721b1854929b748cb2638668b71e5fd77943bd61dc25e1c013aa2b3f890d0bc4a668432058a6b1dd54144cfa256cf2fbe94167b5462ccb46e3fe501c9f4"
//        )
//        //设置删除14天前的日志
//        xlog.setMaxAliveTime(logInstancePtr, 14L * 24L * 60L * 60L)
//        //文件大小最大10M
//        xlog.setMaxFileSize(logInstancePtr, 10L * 1024L * 1024L)
//        Log.setLogImp(xlog)
//    }
//
//    override fun setLogLevel(logType: Int) {
//        super.setLogLevel(logType)
//        val logLevel = getXLogLevel(logType)
//        xlog.appenderOpen(logLevel, Xlog.AppednerModeAsync, cachePath, filePath, "xlog", 0)
//    }
//
//    private fun getXLogLevel(logType: Int): Int {
//        return when (logType) {
//            LogHelper.VERBOSE -> Xlog.LEVEL_VERBOSE
//            LogHelper.DEBUG -> Xlog.LEVEL_DEBUG
//            LogHelper.INFO -> Xlog.LEVEL_INFO
//            LogHelper.WARN -> Xlog.LEVEL_WARNING
//            LogHelper.ERROR -> Xlog.LEVEL_ERROR
//            LogHelper.ASSERT -> Xlog.LEVEL_FATAL
//            else -> Xlog.LEVEL_NONE
//        }
//    }
//
//    override fun log(logType: Int, modelName: String?, tag: String?, msg: String?) {
//        when (logType) {
//            LogHelper.VERBOSE -> Log.v(tag, mLogStrategy!!.format(modelName, tag, msg))
//            LogHelper.DEBUG -> Log.d(tag, mLogStrategy!!.format(modelName, tag, msg))
//            LogHelper.INFO -> Log.i(tag, mLogStrategy!!.format(modelName, tag, msg))
//            LogHelper.WARN -> Log.w(tag, mLogStrategy!!.format(modelName, tag, msg))
//            LogHelper.ERROR -> Log.e(tag, mLogStrategy!!.format(modelName, tag, msg))
//            LogHelper.ASSERT -> Log.f(tag, mLogStrategy!!.format(modelName, tag, msg))
//            else -> {}
//        }
//    }
//
//    override fun log(logType: Int, modelName: String?, tag: String?, throwable: Throwable?) {
//        log(logType, modelName, tag, mLogStrategy!!.format(throwable))
//    }
//
//    override fun log(logType: Int, modelName: String?, tag: String?, json: JSONObject?) {
//        log(logType, modelName, tag, mLogStrategy!!.format(json))
//    }
//
//    override fun log(logType: Int, modelName: String?, tag: String?, jsonArray: JSONArray?) {
//        log(logType, modelName, tag, mLogStrategy!!.format(jsonArray))
//    }
//
//    override fun log(logType: Int, modelName: String?, tag: String?, map: Map<String?, Any?>?) {
//        log(logType, modelName, tag, mLogStrategy!!.format(map))
//    }
//
//    override fun log(logType: Int, modelName: String?, tag: String?, list: List<*>?) {
//        log(logType, modelName, tag, mLogStrategy!!.format(list))
//    }
//
//    companion object {
//        private var filePath: String? = null
//        private var cachePath: String? = null
//    }
//}
//
//class Xlog()
