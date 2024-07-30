package com.soul.bluetooth

import android.app.Application
import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.soul.base.BaseViewModel
import com.soul.bleSDK.BleConnectManager
import com.soul.bleSDK.BleListener
import com.soul.bleSDK.BleSDKManager
import com.soul.bleSDK.interfaces.BaseBleListener


/**
 *     author : yangzy33
 *     time   : 2024-07-04
 *     desc   :
 *     version: 1.0
 */
class BleViewModel(mApplication: Application): BaseViewModel(mApplication) {
    var mBleSDKManager: BleSDKManager? = null
        private set
    private val mReadListener: BleListener by lazy {
        object : BleListener {
            override fun onStart() {
                Log.d(TAG, "onStart: 正在连接...")
            }

            override fun onReceiveData(socket: BluetoothSocket?, msg: String) {
                Log.d(TAG, "onReceiveData: ${socket?.remoteDevice?.name + ": " + msg}")
            }

            override fun onConnected(msg: String) {
                super.onConnected(msg)
                Log.d(TAG, "onConnected: 已连接")
            }

            override fun onFail(error: String) {
                Log.d(TAG, "onFail: 已配对 error = $error")
            }
        }
    }
    private val mWriteListener: BaseBleListener by lazy {
        object : BaseBleListener {
            override fun onSendMsg(socket: BluetoothSocket?, msg: String) {
                Log.d(TAG, "onSendMsg: 我: $msg")
            }

            override fun onFail(error: String) {
                Log.d(TAG, "write onFail: $error")
            }
        }
    }

    var mBleConnectManager: BleConnectManager? = null
        private set

    init {
        mBleSDKManager = BleSDKManager().apply {
            setReadListener(mReadListener)
            setWriteListener(mWriteListener)
        }
        mBleConnectManager = BleConnectManager()
    }

}