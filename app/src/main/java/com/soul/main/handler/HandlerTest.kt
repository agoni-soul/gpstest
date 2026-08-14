package com.soul.main.handler

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Toast
import java.lang.ref.WeakReference

/**
 *
 * @author:     haha
 * @date:       2025/7/31
 * Description: Handler学习
 *
 **/
object HandlerTest {
    private val TAG = javaClass.simpleName

    private val lock = Any()
    private var threadHandler: Handler? = null
    private var looperThread: Thread? = null
    private var mContextRef: WeakReference<Context>? = null

    private fun getHandler(): Handler? = threadHandler

    fun handlerLoop(view: View) {
        // applicationContext：避免弱引用失效前的短暂强引用路径持有 Activity
        mContextRef = WeakReference(view.context.applicationContext)
        synchronized(lock) {
            // 已有存活的 Looper 线程则复用，避免每次 onResume 新建永不退出的线程
            if (looperThread?.isAlive == true) {
                return
            }
            val thread = Thread {
                Looper.prepare()
                val handler = object : Handler(Looper.myLooper()!!) {
                    override fun handleMessage(msg: Message) {
                        Log.i(TAG, "handleMessage: ")
                        val ctx = mContextRef?.get() ?: return
                        // Toast 需在主线程展示
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(ctx, "子线程收到消息", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                synchronized(lock) {
                    threadHandler = handler
                }
                Looper.loop()
                // quit 后清理，允许下次重新启动
                synchronized(lock) {
                    if (threadHandler === handler) {
                        threadHandler = null
                    }
                    if (looperThread === Thread.currentThread()) {
                        looperThread = null
                    }
                }
            }.also {
                it.name = "HandlerTest-Looper"
            }
            looperThread = thread
            thread.start()
        }
    }

    fun sendMessageToThreadHandler(view: View) {
        mContextRef = WeakReference(view.context.applicationContext)
        threadHandler?.sendMessage(Message())
        threadHandler?.sendMessageDelayed(threadHandler?.obtainMessage() ?: Message(), 1000)
    }

    fun test() {
        getHandler()?.postDelayed({ Log.d(TAG, "1000") }, 1000)
        getHandler()?.postDelayed({ Log.d(TAG, "2000") }, 2000)
        Thread.sleep(2000)
        getHandler()?.postDelayed({ Log.d(TAG, "0") }, 0)
    }

    fun destroy() {
        synchronized(lock) {
            val handler = threadHandler
            handler?.removeCallbacksAndMessages(null)
            handler?.looper?.quitSafely()
            mContextRef?.clear()
            mContextRef = null
        }
    }
}
