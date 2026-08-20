package com.haha.main.logMonitor

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.haha.main.logMonitor.UiPerfMonitorConfig.Companion.FILENAME
import com.haha.main.logMonitor.UiPerfMonitorConfig.Companion.LOG_PATH
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import kotlin.math.log

/**
 * @auther: haha
 * @Date:   2025/11/27
 * @Detail:
 */
class LogWriteThread: UiPerfMonitorConfig {
    private val TAG = javaClass.simpleName

    private var mWriteHandler: Handler? = null
    private val FILE_LOCK = Object()
    private val FILE_NAME_FORMATTER = SimpleDateFormat("yyyy-MM-dd")
    private val TIME_FORMATTER = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    /**
     * 追加内容: 使用 RandomAccessFile
     *
     * @param fileName 文件名
     * @param content 追加的内容
     */
    companion object {
        fun writeLog4SameFile(fileName: String, content: String) {
            var randomFile: RandomAccessFile? = null
            try {
                // 打开一个随机访问文件流，按读写方式
                randomFile = RandomAccessFile(fileName, "rw")
                // 文件长度，字节数
                val fileLength = randomFile.length()
                // 将写文件指针移动文件尾
                randomFile.seek(fileLength)
                randomFile.writeBytes(content)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                randomFile?.let {
                    try {
                        it.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun saveLog(logInfo: String) {
        getControlHander().post(Runnable {
            synchronized(FILE_LOCK) {
                saveLog2Local(logInfo)
            }
        })
    }

    private fun saveLog2Local(info: String?) {
        val time = System.currentTimeMillis()
        val logFile = File("$LOG_PATH/$FILENAME-$FILE_NAME_FORMATTER")
        val mSb = StringBuilder("/*****************************************/\n\r\n")
        mSb.append(TIME_FORMATTER.format(time))
        mSb.append("\r\n/**********************************/\r\n")
        mSb.append(info + "\r\n")
        Log.d(TAG, "saveLogTOSDCard: ${mSb}")
        if (!logFile.exists()) {
            writeLog4SameFile(logFile.path, mSb.toString())
        } else {
            var writer: BufferedWriter? = null
            try {
                val out = OutputStreamWriter(FileOutputStream(logFile.path, true), "UTF-8")
                writer = BufferedWriter(out)
                writer.write(mSb.toString())
                writer.flush()
                writer.close()
                writer = null
            } catch (t: Throwable) {
                Log.e(TAG, "saveLogToSDCard: ${t.printStackTrace()}")
            } finally {
                try {
                    writer?.let {
                        it.close()
                        writer = null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "saveLogToSDCard: ${e.printStackTrace()}")
                }
            }
        }
    }

    fun send2Server() {
        getControlHander().post {
            // TODO 上传到服务器
        }
    }

    fun getControlHander(): Handler {
        if (mWriteHandler == null) {
            val mHT = HandlerThread("SampleThread")
            mHT.start()
            mWriteHandler = Handler(mHT.looper)
        }
        return mWriteHandler!!
    }
}