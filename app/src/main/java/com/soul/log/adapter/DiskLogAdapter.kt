package com.soul.log.adapter

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import com.blankj.utilcode.util.FileIOUtils
import com.soul.log.utils.LogFileUtil
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2023/03/28
 *     desc   :
 *     version: 1.0
 * </pre>
 */
open class DiskLogAdapter(filePath: String?) : DefaultLogAdapter() {
    protected var logFilePath: String? = null
    private val mHandler: Handler

    init {
        if (filePath != null && !TextUtils.isEmpty(filePath)) {
            logFilePath = filePath
        } else {
            logFilePath = LogFileUtil.getLogFolder()
        }
        val ht = HandlerThread("DOFLog_write")
        ht.start()
        mHandler = WriteHandler(ht.looper)
    }

    override fun log(logType: Int, modelName: String?, tag: String?, msg: String?) {
        sendMessage(logType, modelName, tag, mLogStrategy!!.format(modelName, tag, msg))
    }

    override fun log(logType: Int, modelName: String?, tag: String?, throwable: Throwable?) {
        log(logType, modelName, tag, mLogStrategy!!.format(throwable))
    }

    override fun log(logType: Int, modelName: String?, tag: String?, json: JSONObject?) {
        log(logType, modelName, tag, mLogStrategy!!.format(json))
    }

    override fun log(logType: Int, modelName: String?, tag: String?, jsonArray: JSONArray?) {
        log(logType, modelName, tag, mLogStrategy!!.format(jsonArray))
    }

    override fun log(logType: Int, modelName: String?, tag: String?, map: Map<String?, Any?>?) {
        log(logType, modelName, tag, mLogStrategy!!.format(map))
    }

    override fun log(logType: Int, modelName: String?, tag: String?, list: List<*>?) {
        log(logType, modelName, tag, mLogStrategy!!.format(list))
    }

    internal inner class WriteHandler(looper: Looper?) : Handler(looper!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val me = msg.obj as MsgEntry
            saveLogToFile(me)
        }
    }

    protected fun saveLogToFile(me: MsgEntry) {
        var namePrefix = "log"
        val processName: String = LogFileUtil.getProcessName()
        if (!TextUtils.isEmpty(processName)) {
            namePrefix = namePrefix + "_" + processName
        }
        val logFile = logFilePath + namePrefix + "_" + format.format(Date()) + ".txt"
        FileIOUtils.writeFileFromString(logFile, me.msg, true)
    }

    class MsgEntry {
        var tag: String? = null
        var modelName: String? = null
        var msg: String? = null
    }

    private fun sendMessage(logType: Int, modelName: String?, tag: String?, msg: String?) {
        val m = mHandler.obtainMessage()
        m.arg1 = logType
        val ms = MsgEntry()
        ms.tag = tag
        ms.modelName = modelName
        ms.msg = msg
        m.obj = ms
        m.sendToTarget()
    }

    companion object {
        private val format = SimpleDateFormat("yyyyMMddHH", Locale.ENGLISH)
    }
}
