package com.soul.blesdk.threads

import android.bluetooth.BluetoothSocket
import com.soul.blesdk.interfaces.BaseBleListener
import com.soul.blesdk.utils.close
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
class WriteThread(
    bleSocket: BluetoothSocket?,
    private val mBleListener: BaseBleListener?
): BaseCommunicateThread(bleSocket) {
    private val dataOutput: OutputStream? = mBleSocket?.outputStream

    fun sendMessage(msg: String?) {
        if (isDone || msg == null) return
        scope.launch(Dispatchers.IO) {
            val result = withContext(Dispatchers.IO) {
                sendScope(msg)
            }
            if (result != null) {
                mBleListener?.onFail(result)
            } else {
                mBleListener?.onSendMsg(mBleSocket, msg)
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

    override fun close() {
        super.close()
        close(mBleSocket, dataOutput)
    }
}