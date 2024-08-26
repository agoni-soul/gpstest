package com.soul.bleSDK.communication

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.content.Context
import androidx.annotation.RequiresPermission
import com.soul.SoulApplication
import com.soul.bleSDK.constants.BleConstants
import com.soul.bleSDK.exceptions.BleErrorException
import com.soul.bleSDK.interfaces.BleGattCallback
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(bleDevice: BluetoothDevice?, context: Context?, autoConnect: Boolean, bleGattCallback: BleGattCallback?) {
        bleDevice ?: return
        context ?: SoulApplication.application ?: return
        val tempContext = context ?: SoulApplication.application
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun readBleMessage() {
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun writeBleMessage(message: String) {
        if (message.isEmpty()) return

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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun closeConnect() {
        mIsConnect = false
        mBleGatt?.apply {
            disconnect()
            close()
        }
    }
}