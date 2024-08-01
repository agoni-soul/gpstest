package com.soul.bleSDK.threads

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.io.Closeable


/**
 *     author : yangzy33
 *     time   : 2024-08-01
 *     desc   :
 *     version: 1.0
 */
abstract class BaseCommunicateThread(bleSocket: BluetoothSocket?): Closeable {
    protected val TAG = this.javaClass::class.simpleName

    protected var isDone = false
    protected val mBleSocket = bleSocket
    private val job = Job()
    protected val scope = CoroutineScope(job)

    override fun close() {
        isDone = true
        job.cancel()
    }
}