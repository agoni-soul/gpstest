package com.soul.flutter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.soul.flutter.FlutterChannel.Companion.MESSAGE_CALCULATE_SUM
import com.soul.flutter.FlutterChannel.Companion.MESSAGE_ERROR_EXCEPTION
import com.soul.flutter.FlutterChannel.Companion.MESSAGE_GET_FLUTTER_DATA
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @auther: haha
 * @Date:   2026/1/7
 * @Detail:
 */
class FlutterChannel(context: Context) {

    private val appContext = context.applicationContext
    private lateinit var flutterEngine: FlutterEngine
    private lateinit var methodChannel: MethodChannel
    private var testJob: Job? = null

    companion object {
        private val TAG = "FlutterChannel"
        private const val CHANNEL_NAME = "com.haha.flutter_module/channel"

        const val MESSAGE_CALCULATE_SUM = 101
        const val MESSAGE_GET_FLUTTER_DATA = 102
        const val MESSAGE_ERROR_EXCEPTION = 103

        val mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                val obj = msg.obj
                when (msg.what) {
                    MESSAGE_CALCULATE_SUM -> {
                        println("计算总和: ${(obj as? Int) ?: 0}")
                    }

                    MESSAGE_GET_FLUTTER_DATA -> {
                        println("Flutter 返回: ${obj ?: "调用失败"}")
                    }

                    MESSAGE_ERROR_EXCEPTION -> {
                        val e = obj as? Exception
                        Log.e(TAG, "message = ${e?.message}")
                    }

                    else -> {
                        println("自定义方法结果: $obj")
                    }
                }
            }
        }
    }

    // 初始化 FlutterEngine（仅使用 applicationContext）
    fun initialize() {
        flutterEngine = FlutterEngine(appContext)

        // 启动 FlutterEngine
        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )

        // 创建 MethodChannel
        methodChannel = MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL_NAME
        )
    }

    fun testFlutterCalls() {
        testJob?.cancel()
        testJob = CoroutineScope(Dispatchers.IO).launch {
            // 示例1：调用简单方法
            getFlutterData("Hello from Android")

            // 示例2：调用计算方法
            val numbers = listOf(1, 2, 3, 4, 5)
            calculateSum(numbers)

            // 示例3：直接调用任意方法
            callFlutterMethod(
                "customMethod",
                mapOf("key" to "value")
            )
        }

    }

    // 调用 Flutter 方法
    suspend fun callFlutterMethod(methodName: String, arguments: Any?) {
        withContext(Dispatchers.Main) {
            try {
                methodChannel.invokeMethod(methodName, arguments, object : MethodChannel.Result {
                    override fun success(result: Any?) {
                        val message = mHandler.obtainMessage()
                        message.what = MessageType.getMessageValue(methodName)
                        message.obj = result
                        mHandler.sendMessage(message)
                    }

                    override fun error(
                        errorCode: String,
                        errorMessage: String?,
                        errorDetails: Any?
                    ) {
                        val message = mHandler.obtainMessage()
                        message.what = MESSAGE_ERROR_EXCEPTION
                        message.obj = Exception("$errorCode - $errorMessage")
                        mHandler.sendMessage(message)
                    }

                    override fun notImplemented() {
                        Log.i(TAG, "notImplemented")
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 获取 Flutter 数据
    suspend fun getFlutterData(input: String) {
        callFlutterMethod("getFlutterData", input)
    }

    // 计算总和
    suspend fun calculateSum(numbers: List<Int>) {
        callFlutterMethod("calculateSum", numbers)
    }

    // 释放资源
    fun destroy() {
        testJob?.cancel()
        testJob = null
        mHandler.removeCallbacksAndMessages(null)
        if (::flutterEngine.isInitialized) {
            flutterEngine.destroy()
        }
    }
}

enum class MessageType(val messageType: String, val value: Int) {
    GetFlutterData("getFlutterData", MESSAGE_GET_FLUTTER_DATA),
    CalculateSum("calculateSum", MESSAGE_CALCULATE_SUM),
    ErrorException("errorException", MESSAGE_ERROR_EXCEPTION);

    companion object {
        fun getMessageValue(messageType: String): Int {
            return when (messageType) {
                GetFlutterData.messageType -> GetFlutterData.value
                CalculateSum.messageType -> CalculateSum.value
                ErrorException.messageType -> ErrorException.value
                else -> ErrorException.value
            }
        }
    }
}