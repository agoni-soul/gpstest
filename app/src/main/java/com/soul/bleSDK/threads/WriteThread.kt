package com.soul.bleSDK.threads

import android.bluetooth.BluetoothSocket
import com.soul.bleSDK.interfaces.BaseBleListener
import com.soul.bleSDK.utils.close
import kotlinx.coroutines.*
import java.io.OutputStream


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
class WriteThread(
    private val socket: BluetoothSocket?,
    val listener: BaseBleListener?
) {
    private var isDone = false
    private val dataOutput: OutputStream? = socket?.outputStream

    private val job = Job()
    private val scope = CoroutineScope(job)

    fun sendMsg(msg: String) {
        if (isDone) return
        scope.launch(Dispatchers.IO) {
            val result = withContext(Dispatchers.IO) {
                sendScope(msg)
            }
            if (result != null) {
                listener?.onFail(result)
            } else {
                listener?.onSendMsg(socket, msg)
            }
        }
    }

    private fun sendScope(msg: String): String? {
        return try {
            // 写数据
            dataOutput?.write(msg.toByteArray())
            dataOutput?.flush()
            null
        } catch (e: java.lang.Exception) {
            e.toString()
        }
    }

    fun cancel() {
        isDone = true
        socket?.close()
        close(dataOutput)
        job.cancel()
    }
}