package com.soul.bluetooth

import android.app.Application
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.soul.base.BaseViewModel
import com.soul.bean.BleScanResult
import com.soul.bleSDK.BleListener
import com.soul.bleSDK.exceptions.BleErrorException
import com.soul.bleSDK.interfaces.BaseBleListener
import com.soul.bleSDK.interfaces.IBleConnectCallback
import com.soul.bleSDK.manager.BleA2dpConnectManager
import com.soul.bleSDK.manager.BleCommunicateManager
import com.soul.bleSDK.manager.BleRfcommConnectManager


/**
 *     author : yangzy33
 *     time   : 2024-07-04
 *     desc   :
 *     version: 1.0
 */
class BleViewModel(mApplication: Application): BaseViewModel(mApplication) {
    var mBleRfcommConnectManager: BleRfcommConnectManager? = null
        private set
    private val mReadListener: BleListener by lazy {
        object : BleListener {
            override fun onStart() {
                Log.d(TAG, "onStart: 正在连接...")
            }

            override fun onReceiveData(socket: BluetoothSocket?, msg: String?) {
                Log.d(TAG, "onReceiveData: ${socket?.remoteDevice?.name + ": " + msg}")
            }

            override fun onConnected(msg: String?) {
                super.onConnected(msg)
                Log.d(TAG, "onConnected: 已连接")
            }

            override fun onFail(error: String?) {
                Log.d(TAG, "onFail: 已配对 error = $error")
            }
        }
    }
    private val mWriteListener: BaseBleListener by lazy {
        object : BaseBleListener {
            override fun onSendMsg(socket: BluetoothSocket?, msg: String?) {
                Log.d(TAG, "onSendMsg: 我: $msg")
            }

            override fun onFail(error: String?) {
                Log.d(TAG, "write onFail: $error")
            }
        }
    }

    var mBleA2dpConnectManager: BleA2dpConnectManager? = null
        private set

    var mBleCommunicateManager: BleCommunicateManager? = null

    init {
        mBleRfcommConnectManager = BleRfcommConnectManager().apply {
            setConnectCallback(object: IBleConnectCallback {
                override fun onStart() {
                    Log.d(TAG, "BleRfcommManager: onStart")
                }

                override fun onConnected(bleScanResult: BleScanResult?) {
                    mBleCommunicateManager = BleCommunicateManager(
                        mBleRfcommConnectManager?.getBluetoothSocket(),
                        mReadListener,
                        mWriteListener
                    ).apply {
                        init()
                        startReadMessage()
                    }
                }

                override fun onFail(e: BleErrorException?) {
                    Log.e(TAG, "BleRfcommManager: onFail: errorMessage = ${e?.message}")
                }

                override fun close() {
                    Log.e(TAG, "BleRfcommManager: close")

                }
            })
        }
        mBleA2dpConnectManager = BleA2dpConnectManager()
    }

}