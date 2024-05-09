package com.soul.log.strategy

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process
import android.text.TextUtils
import com.blankj.utilcode.util.Utils
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
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
class AndroidLogFormat : ILogFormat {
    override fun format(msg: String?): String {
        return format("", "", msg)
    }

    override fun format(modelName: String?, tag: String?, msg: String?): String {
        return """
               ${getInfoTag(modelName, tag)}:$msg
               
               """.trimIndent()
    }

    override fun format(msg: Throwable?): String {
        if (msg == null) {
            return "unknown exception."
        }
        val sb = StringBuilder()
        sb.append(" ")
        sb.append("\n================ Error Start ================\n")
        val cause = msg.cause
        val elements: Array<StackTraceElement>
        if (cause != null) {
            elements = cause.stackTrace
            sb.append(String.format("%s :%s\n", cause.javaClass.name, cause.message))
        } else {
            elements = msg.stackTrace
            sb.append(String.format("%s: %s\n", msg.javaClass.name, msg.message))
        }
        for (element in elements) {
            sb.append(element.className)
            sb.append(".")
            sb.append(element.methodName)
            sb.append("(")
            sb.append(element.fileName)
            sb.append(":")
            sb.append(element.lineNumber)
            sb.append(")\n")
        }
        sb.append("================ Error End ================\n \n")
        return sb.toString()
    }

    override fun format(json: JSONObject?): String {
        return """
               $json
               
               """.trimIndent()
    }

    override fun format(jsonArray: JSONArray?): String {
        return """
               -----------------JsonArrayStart---------------
               $jsonArray
               -----------------JsonArrayEnd-----------------
               
               """.trimIndent()
    }

    override fun format(map: Map<*, *>?): String? {
        if (map == null) return null
        val sb = StringBuilder()
        sb.append("-----------------MapStart---------------")
        sb.append("\n")
        val sets = map.entries
        for (e in sets) {
            sb.append(String.format("|%s", e.toString()))
            sb.append("\n")
        }
        sb.append("-----------------MapEnd---------------\n")
        return sb.toString()
    }

    override fun format(list: List<*>?): String? {
        if (list == null) return null
        val sb = StringBuilder()
        sb.append("-----------------ListStart---------------")
        sb.append("\n")
        var i = 0
        val size = list.size
        while (i < size) {
            val `object` = list[i]!!
            sb.append(String.format("|%s", `object`.javaClass.simpleName))
            sb.append(String.format("[%d]:", i))
            sb.append(`object`.toString())
            sb.append("\n")
            i++
        }
        sb.append("-----------------ListEnd---------------\n")
        return sb.toString()
    }

    companion object {
        /**
         * log 库包名
         */
        private const val PACKAGE_NAME = "com.midea.base.log"

        /**
         * 对外暴露 各组件log工具类名后缀
         */
        private const val CLASS_SUFFIX = "DOFLog"

        private var sProcessName: String? = null

        private fun getInfoTag(modelName: String?, tag: String?): String {
            val autoJumpLogInfos = autoJumpLogInfos
            return "[" + SimpleDateFormat("yyyyMMddHHmmss").format(Date()) + "]" +  // modelName
                    "[" + modelName + "]" +  // tag
                    "[" + tag + "]" +  // fileName
                    "[" + autoJumpLogInfos[0] + "]" +  // methodName
                    "[" + autoJumpLogInfos[1] + "]" +  // line log产生行号
                    "[" + autoJumpLogInfos[2] + "]" +  // pid 进程名
                    "[" + currentProcessName.toString() + "]" +  // tid
                    "[" + Thread.currentThread().name.toString() + "]"
        }

        private val currentProcessName: String?
            private get() {
                if (sProcessName == null) {
                    sProcessName = getCurrentProcessName1()
                }
                return sProcessName
            }

        /**
         * 获取打印信息所在方法名，行号等信息
         * 0 className,
         * 1 方法名
         * 2 行号
         */
        private val autoJumpLogInfos: Array<String>
            private get() {
                val infos = arrayOf("", "", "")
                val elements = Thread.currentThread().stackTrace
                if (elements.size <= 2) {
                    return infos
                }
                for (i in 2 until elements.size) {
                    elements[i].className
                    if (TextUtils.isEmpty(elements[i].className) ||
                        (elements[i].className.contains(PACKAGE_NAME)
                                || elements[i].className.contains(CLASS_SUFFIX))
                    ) {
                        continue
                    }
                    infos[0] = elements[i].fileName
                    infos[1] = elements[i].methodName + "()"
                    infos[2] = elements[i].lineNumber.toString()
                    break
                }
                return infos
            }

        fun getCurrentProcessName1(): String? {
            var name = getCurrentProcessNameByFile()
            if (!TextUtils.isEmpty(name)) return name
            name = getCurrentProcessNameByAms()
            if (!TextUtils.isEmpty(name)) return name
            name = getCurrentProcessNameByReflect()
            return name
        }

        private fun getCurrentProcessNameByFile(): String? {
            return try {
                val file = File("/proc/" + Process.myPid() + "/" + "cmdline")
                val mBufferedReader = BufferedReader(FileReader(file))
                val processName = mBufferedReader.readLine().trim { it <= ' ' }
                mBufferedReader.close()
                processName
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }

        private fun getCurrentProcessNameByAms(): String? {
            try {
                val am =
                    Utils.getApp().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        ?: return ""
                val info = am.runningAppProcesses
                if (info == null || info.size == 0) return ""
                val pid = Process.myPid()
                for (aInfo in info) {
                    if (aInfo.pid == pid) {
                        if (aInfo.processName != null) {
                            return aInfo.processName
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                return ""
            }
            return ""
        }

        private fun getCurrentProcessNameByReflect(): String? {
            var processName = ""
            try {
                val app: Application = Utils.getApp()
                val loadedApkField = app.javaClass.getField("mLoadedApk")
                loadedApkField.isAccessible = true
                val loadedApk = loadedApkField[app]
                val activityThreadField = loadedApk.javaClass.getDeclaredField("mActivityThread")
                activityThreadField.isAccessible = true
                val activityThread = activityThreadField[loadedApk]
                val getProcessName = activityThread.javaClass.getDeclaredMethod("getProcessName")
                processName = getProcessName.invoke(activityThread) as String
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return processName
        }
    }
}

