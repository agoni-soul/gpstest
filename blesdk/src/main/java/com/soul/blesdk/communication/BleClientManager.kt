package com.soul.blesdk.communication

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.content.Context
import androidx.annotation.RequiresPermission
import com.blankj.utilcode.util.Utils
import com.soul.BleSDKApplication
import com.soul.blesdk.constants.BleConstants
import com.soul.blesdk.exceptions.BleErrorException
import com.soul.blesdk.interfaces.BleGattCallback
import com.soul.blesdk.permissions.BleSDkPermissionManager
import java.util.UUID

/**
 *
 * @author haha
 * @date 2024-08-26
 * @version 1.0
 *
 */
class BleClientManager {
    private val TAG = javaClass.simpleName
    private var mIsConnect: Boolean = false
    private var mBleGatt: BluetoothGatt? = null
    private var mBleGattCallback: BleGattCallback? = null

    @SuppressLint("MissingPermission")
    fun connect(bleDevice: BluetoothDevice?, context: Context?, autoConnect: Boolean, bleGattCallback: BleGattCallback?) {
        if (!BleSDkPermissionManager.isGrantBleConnect()) return
        bleDevice ?: return
        val tempContext = context ?: BleSDKApplication.application ?: Utils.getApp() ?: return
        mBleGattCallback = bleGattCallback
        mIsConnect = true
        mBleGatt = bleDevice.connectGatt(tempContext, autoConnect, mBleGattCallback)
    }

    fun isConnected(): Boolean = mIsConnect

    // 获取Gatt服务
    private fun getGattService(uuid: UUID): BluetoothGattService? {
        if (!mIsConnect) {
            mBleGattCallback?.onObtainGattServiceStatus(mBleGatt, BleGattCallback.GATT_STATUS_GATT_NULL)
            return null
        }
        val service = mBleGatt?.getService(uuid)
        if (service == null) {
            mBleGattCallback?.onObtainGattServiceStatus(mBleGatt, BleGattCallback.GATT_STATUS_NO_FIND_SERVICE)
        }
        return service
    }

    @SuppressLint("MissingPermission")
    fun readBleMessage() {
        if (!BleSDkPermissionManager.isGrantBleConnect()) return
        //找到 gatt 服务
        val service = getGattService(BleConstants.UUID_SERVICE)
        if (service != null) {
            val characteristic =
                service.getCharacteristic(BleConstants.UUID_READ_NOTIFY)
            if (characteristic == null) {
                mBleGattCallback?.onReadOrWriteException(mBleGatt, BleErrorException("UUID_READ_NOTIFY: characteristic is Null"))
                return
            }
            mBleGatt?.readCharacteristic(characteristic)
        }
    }

    @SuppressLint("MissingPermission")
    fun writeBleMessage(message: String) {
        if (message.isEmpty()) return
        if (!BleSDkPermissionManager.isGrantBleConnect()) return

        val service = getGattService(BleConstants.UUID_SERVICE)
        if (service != null) {
            val characteristic =
                service.getCharacteristic(BleConstants.UUID_WRITE)
            if (characteristic == null) {
                mBleGattCallback?.onReadOrWriteException(mBleGatt, BleErrorException("UUID_WRITE: characteristic is Null"))
                return
            }
            characteristic.value = message.toByteArray()
            mBleGatt?.writeCharacteristic(characteristic)
        }
    }

    @SuppressLint("MissingPermission")
    fun closeConnect() {
        mIsConnect = false
        if (!BleSDkPermissionManager.isGrantBleConnect()) return
        mBleGatt?.apply {
            disconnect()
            close()
        }
    }
}