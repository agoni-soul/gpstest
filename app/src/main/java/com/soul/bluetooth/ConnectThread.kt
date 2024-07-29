package com.soul.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.util.*


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
class ConnectThread(
    val device: BluetoothDevice, val readListener: BleListener?,
    val writeListener: BaseBleListener?
): Thread() {
    var handleSocket: HandleSocket? = null
    private val socket: BluetoothSocket? by lazy {
        readListener?.onStart()
        //监听该 uuid
        device.createRfcommSocketToServiceRecord(BLUE_UUID)
    }

    override fun run() {
        super.run()
        try {
            socket.run {
                //阻塞等待
                this?.connect()
                //连接成功，拿到服务端设备名
                socket?.remoteDevice?.let { bleDevice ->
                    readListener?.onConnected(bleDevice.name)
                }

                //处理 socket 读写
                handleSocket = HandleSocket(this)
                handleSocket?.start(readListener, writeListener)
            }
        } catch (e: java.lang.Exception) {
            e.message?.let { readListener?.onFail(it) }
        }
    }
}

val BLUE_UUID = UUID.fromString("00001101-2300-1000-8000-00815F9B34FB")