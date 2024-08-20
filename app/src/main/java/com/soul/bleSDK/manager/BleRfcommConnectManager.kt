package com.soul.bleSDK.manager

import android.Manifest
import android.bluetooth.BluetoothSocket
import com.soul.bean.BleScanResult
import com.soul.bean.toBleScanResult
import com.soul.bleSDK.constants.BleConstants
import com.soul.bleSDK.exceptions.BleErrorException
import com.soul.bleSDK.interfaces.IBleConnectCallback
import com.soul.util.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BleRfcommConnectManager(): BaseConnectManager() {
    private var mBleSocket: BluetoothSocket? = null
    private var mBleConnectCallback: IBleConnectCallback? = null

    fun getBluetoothSocket(): BluetoothSocket? = mBleSocket

    fun setConnectCallback(bleConnectCallback: IBleConnectCallback?) {
        mBleConnectCallback = bleConnectCallback
    }

    override fun connect(bleScanResult: BleScanResult?) {
        close()
        bleScanResult ?: return
        BleScanManager.apply {
            if (isScanning()) {
                stopScan()
            }
        }
        mBleConnectCallback?.onStart()
        if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            mBleConnectCallback?.onFail(BleErrorException("Manifest.permission.BLUETOOTH_CONNECT No Grant"))
            return
        }
        mBleSocket = bleScanResult.device?.createRfcommSocketToServiceRecord(BleConstants.BLUE_UUID)
        MainScope().launch(Dispatchers.IO) {
            try {
                mBleSocket?.run {
                    //阻塞等待
                    this.connect()
                    //连接成功，拿到服务端设备名
                    mBleConnectCallback?.onConnected(this.remoteDevice.toBleScanResult())
                }
            } catch (e: Exception) {
                mBleConnectCallback?.onFail(BleErrorException(e.message, e.cause))
            }
        }
    }

    override fun close() {
        mBleSocket?.close()
    }
}