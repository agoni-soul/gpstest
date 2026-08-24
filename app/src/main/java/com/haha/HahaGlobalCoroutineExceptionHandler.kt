package com.haha

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * 通过 ServiceLoader 注册的全局协程异常处理器。
 * SPI 文件：META-INF/services/kotlinx.coroutines.CoroutineExceptionHandler
 *
 * 仅在协程 Context 中没有局部 CoroutineExceptionHandler 时才会被调用。
 * 调用后仍会走到线程的 UncaughtExceptionHandler，Android 上默认仍可能崩溃。
 */
class HahaGlobalCoroutineExceptionHandler : CoroutineExceptionHandler {

    override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        val coroutineName = context[CoroutineName]?.name ?: "unnamed"
        val job = context[Job]
        Log.e(
            TAG,
            "coroutine=$coroutineName \njob=$job " +
                "\ntype=${exception.javaClass.name} \nmsg=${exception.message} " +
                "\ncause=${exception.cause} \nsuppressed=${exception.suppressed.contentToString()}",
            exception
        )
    }

    companion object {
        private const val TAG = "HahaGlobalCEH"
    }
}
