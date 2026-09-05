package com.haha.log.adapter

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import com.blankj.utilcode.util.FileIOUtils
import com.haha.log.utils.LogFileUtil
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2023/03/28
 *     desc   : 异步写入本地日志文件
 *     version: 1.1
 * </pre>
 */
open class DiskLogAdapter(filePath: String?) : DefaultLogAdapter() {
    protected var logFilePath: String? = null
    private val mHandler: Handler

    init {
        logFilePath = if (!filePath.isNullOrEmpty()) {
            filePath
        } else {
            LogFileUtil.getLogFolder()
        }
        LogFileUtil.ensureDir(logFilePath)
        val ht = HandlerThread("DOFLog_write")
        ht.start()
        mHandler = WriteHandler(ht.looper)
    }

    override fun log(logType: Int, modelName: String?, tag: String?, msg: String?) {
        sendMessage(logType, modelName, tag, mLogStrategy?.format(modelName, tag, msg))
    }

    override fun log(logType: Int, modelName: String?, tag: String?, throwable: Throwable?) {
        log(logType, modelName, tag, mLogStrategy?.format(throwable))
    }

    override fun log(logType: Int, modelName: String?, tag: String?, json: JSONObject?) {
        log(logType, modelName, tag, mLogStrategy?.format(json))
    }

    override fun log(logType: Int, modelName: String?, tag: String?, jsonArray: JSONArray?) {
        log(logType, modelName, tag, mLogStrategy?.format(jsonArray))
    }

    override fun log(logType: Int, modelName: String?, tag: String?, map: Map<String?, Any?>?) {
        log(logType, modelName, tag, mLogStrategy?.format(map))
    }

    override fun log(logType: Int, modelName: String?, tag: String?, list: List<*>?) {
        log(logType, modelName, tag, mLogStrategy?.format(list))
    }

    internal inner class WriteHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            val obj = msg.obj
            if (obj is MsgEntry) {
                saveLogToFile(obj)
            } else if (obj is CountDownLatch) {
                obj.countDown()
            }
        }
    }

    protected fun saveLogToFile(me: MsgEntry) {
        val content = me.msg
        if (content.isNullOrEmpty()) {
            return
        }
        try {
            LogFileUtil.ensureDir(logFilePath)
            val logFile = buildLogFilePath()
            val line = if (content.endsWith("\n")) content else content + "\n"
            FileIOUtils.writeFileFromString(logFile, line, true)
        } catch (_: Exception) {
        }
    }

    fun saveLogToFileSync(msg: String?) {
        if (msg.isNullOrEmpty()) {
            return
        }
        val entry = MsgEntry().apply { this.msg = msg }
        saveLogToFile(entry)
    }

    private fun buildLogFilePath(): String {
        var namePrefix = "log"
        val processName: String = LogFileUtil.getProcessName()
        if (!TextUtils.isEmpty(processName)) {
            namePrefix = namePrefix + "_" + processName
        }
        return (logFilePath ?: "") + namePrefix + "_" + format.format(Date()) + ".txt"
    }

    class MsgEntry {
        var tag: String? = null
        var modelName: String? = null
        var msg: String? = null
    }

    private fun sendMessage(logType: Int, modelName: String?, tag: String?, msg: String?) {
        if (msg.isNullOrEmpty()) {
            return
        }
        val message = mHandler.obtainMessage()
        message.arg1 = logType
        val entry = MsgEntry()
        entry.tag = tag
        entry.modelName = modelName
        entry.msg = msg
        message.obj = entry
        mHandler.sendMessage(message)
    }

    override fun flush(timeoutMs: Long) {
        val latch = CountDownLatch(1)
        val message = mHandler.obtainMessage()
        message.obj = latch
        mHandler.sendMessage(message)
        try {
            latch.await(timeoutMs, TimeUnit.MILLISECONDS)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    companion object {
        private val format = SimpleDateFormat("yyyyMMddHH", Locale.ENGLISH)
    }
}
