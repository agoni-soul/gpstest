package com.soul.blesdk.communication

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.soul.blesdk.permissions.BleSDkPermissionManager
import java.util.UUID


/**
 *     author : haha
 *     time   : 2024-08-22
 *     desc   :
 *     version: 1.0
 */
class BleServerImpl(
    private val mContext: Context,
    private val mBleAdapter: BluetoothAdapter,
    private val mBleManager: BluetoothManager
) {
    private val TAG = "BleServerImpl"

    private var mBleGattServer: BluetoothGattServer? = null
    private var mGattService: BluetoothGattService? = null
    private val mGattServerCallbackMap = mutableMapOf<String, BluetoothGattServerCallback>()
    private val mGattServiceCallback: BluetoothGattServerCallback by lazy {
        object : BluetoothGattServerCallback() {
            @SuppressLint("MissingPermission")
            override fun onConnectionStateChange(
                device: BluetoothDevice?,
                status: Int,
                newState: Int
            ) {
                Log.d(TAG, "onConnectionStateChange")
                mGattServerCallbackMap.forEach { (_, callback) ->
                    callback.onConnectionStateChange(device, status, newState)
                }
            }

            @SuppressLint("MissingPermission")
            override fun onCharacteristicReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic?
            ) {
                Log.d(TAG, "onCharacteristicReadRequest")
                /**
                 * 中心设备read时，回调
                 */
                val data = "this is a test from ble server"
                mBleGattServer?.sendResponse(
                    device, requestId, BluetoothGatt.GATT_SUCCESS,
                    offset, data.toByteArray()
                )
                mGattServerCallbackMap.forEach { (_, callback) ->
                    callback.onCharacteristicReadRequest(device, requestId, offset, characteristic)
                }
            }

            @SuppressLint("MissingPermission")
            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice?,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic?,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?
            ) {
                Log.d(TAG, "onCharacteristicWriteRequest")
                mBleGattServer?.sendResponse(
                    device, requestId, BluetoothGatt.GATT_SUCCESS,
                    offset, value
                )
                mGattServerCallbackMap.forEach { (_, callback) ->
                    callback.onCharacteristicWriteRequest(
                        device,
                        requestId,
                        characteristic,
                        preparedWrite,
                        responseNeeded,
                        offset,
                        value
                    )
                }
            }

            @SuppressLint("MissingPermission")
            override fun onDescriptorReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                descriptor: BluetoothGattDescriptor?
            ) {
                Log.d(TAG, "onDescriptorReadRequest")
                val data = "this is a test"
                mBleGattServer?.sendResponse(
                    device, requestId, BluetoothGatt.GATT_SUCCESS,
                    offset, data.toByteArray()
                )
                mGattServerCallbackMap.forEach { (_, callback) ->
                    callback.onDescriptorReadRequest(device, requestId, offset, descriptor)
                }
            }

            override fun onDescriptorWriteRequest(
                device: BluetoothDevice?,
                requestId: Int,
                descriptor: BluetoothGattDescriptor?,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?
            ) {
                Log.d(TAG, "onDescriptorWriteRequest")
                mGattServerCallbackMap.forEach { (_, callback) ->
                    callback.onDescriptorWriteRequest(
                        device,
                        requestId,
                        descriptor,
                        preparedWrite,
                        responseNeeded,
                        offset,
                        value
                    )
                }
            }

            override fun onExecuteWrite(
                device: BluetoothDevice?,
                requestId: Int,
                execute: Boolean
            ) {
                Log.d(TAG, "onExecuteWrite")
                mGattServerCallbackMap.forEach { (_, callback) ->
                    callback.onExecuteWrite(device, requestId, execute)
                }
            }

            override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
                Log.d(TAG, "onNotificationSent")
                mGattServerCallbackMap.forEach { (_, callback) ->
                    callback.onNotificationSent(device, status)
                }
            }

            override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
                Log.d(TAG, "onMtuChanged")
                mGattServerCallbackMap.forEach { (_, callback) ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        callback.onMtuChanged(device, mtu)
                    }
                }
            }
        }
    }
    private var mIsAddAdvertising = false

    fun setGattServiceCallback(tag: String, gattServerCallback: BluetoothGattServerCallback?) {
        gattServerCallback?.let {
            mGattServerCallbackMap[tag] = it
        }
    }

    @SuppressLint("missingPermission")
    fun createAndAddBleService(
        isAddAdvertising: Boolean,
        serviceUuid: UUID,
        serviceType: Int,
        readUuid: UUID,
        writeUuid: UUID,
        describeUuid: UUID? = null
    ) {
        Log.d(TAG, "createAndAddBleService")
        mIsAddAdvertising = isAddAdvertising
        // Manifest.permission.BLUETOOTH_ADVERTISE
        if (mIsAddAdvertising &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            BleSDkPermissionManager.isGrantNecessaryBle()) {
            Log.d(TAG, "createAndAddBleService: launchAdvertising")
            BleServerManager.launchAdvertising(mBleAdapter)
        }
        mGattService = createBleGattService(serviceUuid, serviceType)
        // Manifest.permission.BLUETOOTH_CONNECT
        mGattService.apply {
            createReadGattCharacteristic(readUuid)
            createWriteGattCharacteristic(writeUuid, describeUuid)
            Log.d(TAG, "createAndAddBleService: openGattServer")
            // TODO 补上低版本的蓝牙通信逻辑
            if (BleSDkPermissionManager.isGrantBleConnect()) {
                mBleGattServer = mBleManager.openGattServer(mContext, mGattServiceCallback)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Log.d(TAG, "createAndAddBleService: addService")
                    BleServerManager.addService(mBleGattServer, this)
                }
            }
        }
    }

    private fun createBleGattService(serviceUuid: UUID, serviceType: Int): BluetoothGattService {
        Log.d(TAG, "createBleGattService")
        return BleServerManager.getBleGattService(serviceUuid, serviceType)
    }

    private fun createReadGattCharacteristic(readUuid: UUID): BluetoothGattCharacteristic {
        Log.d(TAG, "createReadGattCharacteristic")
        return BleServerManager.createReadGattCharacteristic(mGattService, readUuid)
    }

    private fun createWriteGattCharacteristic(
        writeUuid: UUID,
        describeUuid: UUID?
    ): BluetoothGattCharacteristic {
        Log.d(TAG, "createWriteGattCharacteristic")
        return BleServerManager.createWriteGattCharacteristic(mGattService, writeUuid, describeUuid)
    }


    @SuppressLint("MissingPermission")
    fun close(tag: String) {
        Log.d(TAG, "close")
        // Manifest.permission.BLUETOOTH_ADVERTISE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            BleSDkPermissionManager.isGrantNecessaryBle()) {
            Log.d(TAG, "close: stopAdvertising")
            BleServerManager.stopAdvertising(mBleAdapter)
        }
        mGattServerCallbackMap.remove(tag)
        // Manifest.permission.BLUETOOTH_CONNECT
        if (mGattServerCallbackMap.isEmpty() && BleSDkPermissionManager.isGrantBleConnect()) {
            Log.d(TAG, "close: close")
            mBleGattServer?.close()
        }
    }
}