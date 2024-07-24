package com.soul.bluetooth

import android.os.Handler
import android.os.HandlerThread
import android.os.Message

/**
 *     author : yangzy33
 *     time   : 2024-07-23
 *     desc   :
 *     version: 1.0
 */
class BleConnect {
    //子线程的HandlerThread，为子线程提供Looper
    private var workHandlerThread: HandlerThread? = null

    //子线程
    private var workHandler: Handler? = null

    private fun initWorkHandler() {
        workHandlerThread = HandlerThread("BleWorkHandlerThread")
        workHandlerThread!!.start()
        workHandler = object : Handler(workHandlerThread!!.looper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
            }
        }
    }
}