package com.soul.bleSDK.commication

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.util.Log
import com.soul.util.PermissionUtils
import java.util.*


/**
 *     author : yangzy33
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
        if (mIsAddAdvertising &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_ADVERTISE)) {
            Log.d(TAG, "createAndAddBleService: launchAdvertising")
            BleServerManager.launchAdvertising(mBleAdapter)
        }
        mGattService = createBleGattService(serviceUuid, serviceType)
        mGattService.apply {
            createReadGattCharacteristic(readUuid)
            createWriteGattCharacteristic(writeUuid, describeUuid)
            Log.d(TAG, "createAndAddBleService: openGattServer")
            mBleGattServer = mBleManager.openGattServer(mContext, mGattServiceCallback)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                Log.d(TAG, "createAndAddBleService: addService")
                BleServerManager.addService(mBleGattServer, this)
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

    fun close(tag: String) {
        Log.d(TAG, "close")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_ADVERTISE)) {
            Log.d(TAG, "close: stopAdvertising")
            BleServerManager.stopAdvertising(mBleAdapter)
        }
        mGattServerCallbackMap.remove(tag)
        if (mGattServerCallbackMap.isEmpty() &&
            PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Log.d(TAG, "close: close")
            mBleGattServer?.close()
        }
    }
}