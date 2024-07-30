package com.soul.bleSDK

import android.bluetooth.BluetoothDevice
import com.soul.bleSDK.interfaces.BaseBleListener
import com.soul.bleSDK.interfaces.IBleCallback
import com.soul.bleSDK.threads.ConnectThread

class BleSDKManager: IBleCallback {
    private var mConnectThread: ConnectThread? = null
    private var mReadListener: BleListener? = null
    private var mWriteListener: BaseBleListener? = null

    override fun sendMsg(msg: String?) {
        mConnectThread?.mHandleSocket?.sendMsg(msg)
    }

    override fun close() {
        mConnectThread?.close()
    }

    override fun start(bluetoothDevice: BluetoothDevice?) {
        mConnectThread = ConnectThread(bluetoothDevice, mReadListener, mWriteListener)
        mConnectThread?.start()
    }

    override fun setReadListener(readListener: BleListener?) {
        mReadListener = readListener
    }

    override fun setWriteListener(writeListener: BaseBleListener?) {
        mWriteListener = writeListener
    }
}