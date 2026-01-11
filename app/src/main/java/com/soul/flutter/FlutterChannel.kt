package com.soul.flutter

import android.content.Context
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @auther: haha
 * @Date:   2026/1/7
 * @Detail:
 */
class FlutterChannel(private val context: Context) {

    private lateinit var flutterEngine: FlutterEngine
    private lateinit var methodChannel: MethodChannel

    companion object {
        private const val CHANNEL_NAME = "com.haha.flutter_module/channel"
    }

    // 初始化 FlutterEngine
    fun initialize() {
        flutterEngine = FlutterEngine(context)

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
        CoroutineScope(Dispatchers.Main).launch {
            // 示例1：调用简单方法
            val result1 = getFlutterData("Hello from Android")
            println("Flutter 返回: $result1")

            // 示例2：调用计算方法
            val numbers = listOf(1, 2, 3, 4, 5)
            val sum = calculateSum(numbers)
            println("计算总和: $sum")

            // 示例3：直接调用任意方法
            val customResult = callFlutterMethod(
                "customMethod",
                mapOf("key" to "value")
            )
            println("自定义方法结果: $customResult")
        }

    }

    // 调用 Flutter 方法
    suspend fun callFlutterMethod(methodName: String, arguments: Any?): Any? {
        return withContext(Dispatchers.IO) {
            try {
                methodChannel.invokeMethod(methodName, arguments)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // 获取 Flutter 数据
    suspend fun getFlutterData(input: String): String {
        val result = callFlutterMethod("getFlutterData", input)
        return result as? String ?: "调用失败"
    }

    // 计算总和
    suspend fun calculateSum(numbers: List<Int>): Int {
        val result = callFlutterMethod("calculateSum", numbers)
        return (result as? Number)?.toInt() ?: 0
    }

    // 释放资源
    fun destroy() {
        flutterEngine.destroy()
    }
}