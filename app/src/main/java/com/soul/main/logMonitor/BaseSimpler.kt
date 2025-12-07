package com.soul.main.logMonitor

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @auther: soulagoni
 * @Date:   2025/11/27
 * @Detail:
 */
abstract class BaseSimpler {
    protected val TAG = javaClass.simpleName
    private var mControlHandler: Handler? = null
    private val intervalTime = 500L // ms采样间隔
    private val mIsSampling = AtomicBoolean(false)

    // 采样抽象方法
    abstract fun doSample()

    private val mRunnable = Runnable {
        doSample()
        if (mIsSampling.get()) {
            postDelayed()
        }
    }

    private fun postDelayed() {
        getControlHandler().postDelayed(mRunnable, intervalTime)
    }

    constructor() {
        Log.d(TAG, "Init BaseSampler")
    }

    open fun start() {
        if (!mIsSampling.get()) {
            Log.d(TAG, "start Sampler")
            getControlHandler().removeCallbacks(mRunnable)
            getControlHandler().post(mRunnable)
            mIsSampling.set(true)
        }
    }

    open fun stop() {
        if (mIsSampling.get()) {
            Log.d(TAG, "stop Sampler")
            getControlHandler().removeCallbacks(mRunnable)
            mIsSampling.set(false)
        }
    }

    // 创建一个HandlerThread, 定时采样
    private fun getControlHandler(): Handler {
        if (mControlHandler == null) {
            val mHT = HandlerThread("SamplerThread")
            mHT.start()
            mControlHandler = Handler(mHT.looper)
        }
        return mControlHandler!!
    }
}