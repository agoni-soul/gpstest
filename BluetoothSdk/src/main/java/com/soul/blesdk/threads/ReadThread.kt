package com.soul.blesdk.threads

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.soul.blesdk.interfaces.BleListener
import com.soul.blesdk.interfaces.BaseBleListener
import com.soul.blesdk.utils.close
import kotlinx.coroutines.*
import java.io.DataInputStream


/**
 *     author : haha
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
class ReadThread(
    bleSocket: BluetoothSocket?,
    bleListener: BaseBleListener?
): BaseCommunicateThread(bleSocket) {

    private val mInputStream: DataInputStream? = bleSocket?.inputStream?.let { DataInputStream(it) }
    private val mBleListener: BleListener? = bleListener as? BleListener
    //TODO 目前简单数据，暂时使用这种
    private val mByteBuffer: ByteArray = ByteArray(1024)

    fun startReadMessage() {
        scope.launch(Dispatchers.IO) {
            var size: Int?
            while (!isDone) {
                if (isDone) return@launch
                try {
                    //拿到读的数据和大小
                    size = mInputStream?.read(mByteBuffer)
                } catch (e: java.lang.Exception) {
                    isDone = false
                    e.message?.let {
                        mBleListener?.onFail(it)
                    }
                    return@launch
                }

                Log.d(TAG, "startReadMessage: size = $size")
                if (size != null && size > 0) {
                    // 把结果公布出去
                    mBleListener?.onReceiveData(mBleSocket, String(mByteBuffer, 0, size))
                } else {
                    //如果接收不到数据，则证明已经断开了
                    mBleListener?.onFail("断开连接")
                    return@launch
                }
            }
        }
    }

    override fun close() {
        super.close()
        close(mBleSocket, mInputStream)
    }
}