package com.soul.main.handler

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Toast

/**
 *
 * @author:     haha
 * @date:       2025/7/31
 * Description: Handler学习
 *
 **/
object HandlerTest {
    private val TAG = javaClass.simpleName

    private var threadHandler: Handler? = null

    private fun getHandler(): Handler? = threadHandler

    fun handlerLoop(view: View) {
        val myThread = Thread {
            Looper.prepare()
            threadHandler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    Log.i(TAG, "handleMessage: ")
                    Toast.makeText(view.context, "子线程收到消息", Toast.LENGTH_SHORT).show()
                }
            }
            Looper.loop()
        }
        myThread.start()
    }

    fun sendMessageToThreadHandler(view: View) {
        threadHandler?.sendMessage(Message())
        threadHandler?.sendMessageDelayed(threadHandler?.obtainMessage() ?: Message(), 1000)
    }

    fun test() {
        getHandler()?.postDelayed({ Log.d(TAG, "1000") }, 1000)
        getHandler()?.postDelayed({ Log.d(TAG, "2000") }, 2000)
        Thread.sleep(2000)
        val message = Message()
        getHandler()?.postDelayed({ Log.d(TAG, "0") }, 0)
    }
}