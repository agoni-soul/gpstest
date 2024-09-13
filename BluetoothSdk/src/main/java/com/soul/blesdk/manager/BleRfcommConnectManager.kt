package com.soul.blesdk.manager

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import com.soul.blesdk.bean.BleScanResult
import com.soul.blesdk.bean.toBleScanResult
import com.soul.blesdk.constants.BleConstants
import com.soul.blesdk.exceptions.BleErrorException
import com.soul.blesdk.permissions.BleSDkPermissionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BleRfcommConnectManager : BaseConnectManager() {
    private var mBleSocket: BluetoothSocket? = null

    fun getBluetoothSocket(): BluetoothSocket? = mBleSocket

    @SuppressLint("MissingPermission")
    override fun connect(bleScanResult: BleScanResult?) {
        close()
        bleScanResult ?: return
        mBleConnectCallback?.onStart()
        if (!BleSDkPermissionManager.isGrantConnectRelatedPermissions()) {
            mBleConnectCallback?.onFail(BleErrorException("Lack Necessary Permissions"))
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