package com.soul.bleSDK.threads

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.soul.bleSDK.BleListener
import com.soul.bleSDK.HandleSocket
import com.soul.bleSDK.interfaces.BaseBleListener
import java.util.*
import com.soul.bleSDK.utils.close


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
class ConnectThread(
    val device: BluetoothDevice?,
    val readListener: BleListener?,
    val writeListener: BaseBleListener?
): Thread() {
    var mHandleSocket: HandleSocket? = null
    private val mBleSocket: BluetoothSocket? by lazy {
        readListener?.onStart()
        //监听该 uuid
        device?.createRfcommSocketToServiceRecord(BLUE_UUID)
    }

    override fun run() {
        super.run()
        try {
            mBleSocket?.run {
                //阻塞等待
                this.connect()
                //连接成功，拿到服务端设备名
                this.remoteDevice?.let { bleDevice ->
                    readListener?.onConnected(bleDevice.name)
                }

                //处理 socket 读写
                mHandleSocket = HandleSocket(this)
                mHandleSocket?.start(readListener, writeListener)
            }
        } catch (e: java.lang.Exception) {
            e.message?.let { readListener?.onFail(it) }
        }
    }

    fun close() {
        close(mHandleSocket)
    }
}

val BLUE_UUID = UUID.fromString("00001101-2300-1000-8000-00815F9B34FB")