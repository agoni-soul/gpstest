package com.soul.bleSDK.manager

import android.Manifest
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.soul.appLike.SoulAppLike
import com.soul.bean.BleScanResult
import com.soul.util.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/**
 *     author : yangzy33
 *     time   : 2024-07-30
 *     desc   :
 *     version: 1.0
 */
class BleA2dpConnectManager(): BaseConnectManager() {
    protected var mBleA2dp: BluetoothA2dp? = null
    protected var mBleSocket: BluetoothSocket? = null
    protected var mBleResult: BleScanResult? = null

    init {
        BleScanManager.getBluetoothAdapter()?.getProfileProxy(SoulAppLike.application, object: BluetoothProfile.ServiceListener {
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

    override fun connect(bleScanResult: BleScanResult?) {
        close()
        bleScanResult ?: return
        BleScanManager.cancelDiscovery()
        mBleResult = bleScanResult
        MainScope().launch(Dispatchers.IO) {
            try {
                if (bleScanResult.bondState != BluetoothDevice.BOND_BONDED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                            Log.e(TAG, "BLUETOOTH_CONNECT permission is no grant")
                            return@launch
                        }
                    } else {
                        if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_ADMIN)) {
                            Log.e(TAG, "BLUETOOTH_ADMIN permission is no grant")
                            return@launch
                        }
                    }
                    val createSocket = BluetoothDevice::class.java.getMethod(
                        "createRfcommSocket",
                        Int::class.java
                    )
                    createSocket.isAccessible = true

                    //找一个通道去连接即可，channel 1～30
                    mBleSocket = createSocket.invoke(mBleResult!!.device, 1) as BluetoothSocket
                    //阻塞等待
                    mBleSocket?.connect()
                }

                if (connectA2dp(bleScanResult.device)) {
                    Log.d(TAG, "initView: connect success")
                } else {
                    Log.d(TAG, "initView: connect fail")
                }
            } catch (e: Exception) {
                Log.e(TAG, "initView: connect Error: msg = $e")
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

    override fun close() {
        try {
            mBleSocket?.close()
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