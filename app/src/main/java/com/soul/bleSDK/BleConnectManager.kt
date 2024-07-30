package com.soul.bleSDK

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.util.Log
import com.soul.appLike.SoulAppLike
import com.soul.bean.BleScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 *     author : yangzy33
 *     time   : 2024-07-30
 *     desc   :
 *     version: 1.0
 */
class BleConnectManager: BleScanManager() {
    protected var mBleA2dp: BluetoothA2dp? = null
    protected var socket: BluetoothSocket? = null
    protected var mBleResult: BleScanResult? = null

    init {
        mBleAdapter?.getProfileProxy(SoulAppLike.application, object: BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                if (profile == BluetoothProfile.A2DP && proxy is BluetoothA2dp) {
                    mBleA2dp = proxy
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                if (profile == BluetoothProfile.A2DP) {
                    mBleA2dp = null
                }
            }

        }, BluetoothProfile.A2DP)
    }

    fun getBluetoothA2dp(): BluetoothA2dp? = mBleA2dp

    fun connect(result: BleScanResult?) {
        close()
        result ?: return
        mBleResult = result
        SoulAppLike.homeScope?.launch(Dispatchers.IO) {
            val retryAmount = 10
            var retryCount = 0
            while (retryCount <= retryAmount) {
                try {
                    if (result.bondState != BluetoothDevice.BOND_BONDED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val createSocket = BluetoothDevice::class.java.getMethod(
                                "createRfcommSocket",
                                Int::class.java
                            )
                            createSocket.isAccessible = true

                            //找一个通道去连接即可，channel 1～30
                            socket =
                                createSocket.invoke(mBleResult!!.device, 1) as BluetoothSocket
                            //阻塞等待
                            socket?.connect()
                            //延时，以便于去连接
                            Thread.sleep(2000)
                        } else {
                            break
                        }
                    }

                    if (connectA2dp(result.device)) {
                        Log.d(TAG, "initView: connect success")
                    } else {
                        Log.d(TAG, "initView: connect fail")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "initView: connect Error: msg = $e")
                }
                retryCount++
            }
        }
    }

    private fun connectA2dp(device: BluetoothDevice?): Boolean {
        device ?: return false
        //连接 a2dp
        val connect = BluetoothA2dp::class.java.getMethod("connect", BluetoothDevice::class.java)
        connect.isAccessible = true
        return connect.invoke(mBleA2dp, device) as Boolean
    }

    fun close() {
        try {
            socket?.close()
            try {
                //通过反射获取BluetoothA2dp中connect方法（hide的），断开连接。
                val connectMethod = BluetoothA2dp::class.java.getMethod(
                    "disconnect",
                    BluetoothDevice::class.java
                )
                connectMethod.invoke(mBleA2dp, mBleResult?.device)
            } catch (e: Exception) {
                Log.e(TAG, "close: disconnect: error = ${e.message}")
                e.printStackTrace()
            }
        } catch (e: Exception) {
            Log.e(TAG, "close: close: error = ${e.message}")
            e.printStackTrace()
        }
    }
}