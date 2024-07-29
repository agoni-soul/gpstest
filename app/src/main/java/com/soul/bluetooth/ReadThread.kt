package com.soul.bluetooth

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.DataInputStream


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
class ReadThread(val socket: BluetoothSocket?, bleListener: BaseBleListener?): Thread() {
    companion object {
        private val TAG = ReadThread::class.java.simpleName
    }

    private val mInputStream: DataInputStream? = socket?.inputStream?.let { DataInputStream(it) }
    private var isDone = false
    private val listener: BleListener? = bleListener as? BleListener
    //TODO 目前简单数据，暂时使用这种
    private val mByteBuffer: ByteArray = ByteArray(1024)

    override fun run() {
        super.run()
        var size: Int? = null
        while (!isDone) {
            try {
                //拿到读的数据和大小
                size = mInputStream?.read(mByteBuffer)
            } catch (e: java.lang.Exception) {
                isDone = false
                e.message?.let {
                    listener?.onFail(it)
                }
                return
            }

            Log.d(TAG, "run: size = $size")
            if (size != null && size > 0) {
                // 把结果公布出去
                listener?.onReceiveData(socket, String(mByteBuffer, 0, size))
            } else {
                //如果接收不到数据，则证明已经断开了
                listener?.onFail("断开连接")
                isDone = false
            }
        }
    }

    fun cancel() {
        isDone = false
        socket?.close()
        close(mInputStream)
    }
}