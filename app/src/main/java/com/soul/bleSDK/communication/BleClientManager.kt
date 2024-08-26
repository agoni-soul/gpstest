package com.soul.bleSDK.communication

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.soul.SoulApplication
import com.soul.bleSDK.constants.BleBlueImpl
import com.soul.util.PermissionUtils
import java.util.UUID


/**
 *     author : yangzy33
 *     time   : 2024-08-22
 *     desc   :
 *     version: 1.0
 */
class BleClientManager {
    private val TAG = javaClass.simpleName
    private var mIsConnect: Boolean = false
    private var mBleGatt: BluetoothGatt? = null

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(bleDevice: BluetoothDevice?, autoConnect: Boolean, bleGattCallback: BluetoothGattCallback?) {
        bleDevice ?: return
        SoulApplication.application ?: return
        mIsConnect = true
        mBleGatt = bleDevice.connectGatt(SoulApplication.application, autoConnect, bleGattCallback)
    }

    // 获取Gatt服务
    private fun getGattService(uuid: UUID): BluetoothGattService? {
        if (!mIsConnect) {
            Toast.makeText(SoulApplication.application, "没有连接", Toast.LENGTH_SHORT).show()
            return null
        }
        val service = mBleGatt?.getService(uuid)
        if (service == null) {
            Toast.makeText(SoulApplication.application, "没有找到服务", Toast.LENGTH_SHORT).show()
        }
        return service
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun readBleMessage() {
        //找到 gatt 服务
        val service = getGattService(BleBlueImpl.UUID_SERVICE)
        if (service != null) {
            val characteristic =
                service.getCharacteristic(BleBlueImpl.UUID_READ_NOTIFY)
            if (characteristic == null) {
                Log.d(TAG, "readData: characteristic is Null")
                return
            }
            mBleGatt?.readCharacteristic(characteristic)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun writeBleMessage(message: String) {
        if (message.isEmpty()) return

        val service = getGattService(BleBlueImpl.UUID_SERVICE)
        if (service != null) {
            val characteristic =
                service.getCharacteristic(BleBlueImpl.UUID_WRITE) //通过UUID获取可读的Characteristic
            if (characteristic == null) {
                Log.d(TAG, "writeData: characteristic is Null")
                return
            }
            characteristic.value = message.toByteArray()
            mBleGatt?.writeCharacteristic(characteristic)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun closeConnect() {
        mBleGatt?.apply {
            disconnect()
            close()
        }
    }
}