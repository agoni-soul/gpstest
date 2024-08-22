package com.soul.bleSDK.commication

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    bleManager: BluetoothManager
) {
    private var mBleGattServer: BluetoothGattServer? = null
    private var mGattService: BluetoothGattService? = null
    private val mGattServerCallbackMap = mutableMapOf<String, BluetoothGattServerCallback>()
    private val mGattServiceCallback: BluetoothGattServerCallback by lazy {
        object : BluetoothGattServerCallback() {
            override fun onConnectionStateChange(
                device: BluetoothDevice?,
                status: Int,
                newState: Int
            ) {
                mGattServerCallbackMap.forEach { (_, callback) ->
                    callback.onConnectionStateChange(device, status, newState)
                }
            }


            override fun onCharacteristicReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic?
            ) {
                mGattServerCallbackMap.forEach { (_, callback) ->
                    callback.onCharacteristicReadRequest(device, requestId, offset, characteristic)
                }
            }

            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice?,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic?,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?
            ) {
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

            override fun onDescriptorReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                descriptor: BluetoothGattDescriptor?
            ) {
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
                mGattServerCallbackMap.forEach { (_, callback) ->
                    callback.onExecuteWrite(device, requestId, execute)
                }
            }

            override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
                mGattServerCallbackMap.forEach { (_, callback) ->
                    callback.onNotificationSent(device, status)
                }
            }

            override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
                mGattServerCallbackMap.forEach { (_, callback) ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        callback.onMtuChanged(device, mtu)
                    }
                }
            }
        }
    }
    private var mIsAddAdvertising = false

    init {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) ==
            PackageManager.PERMISSION_DENIED
        ) {
            mBleGattServer = bleManager.openGattServer(mContext, mGattServiceCallback)
        }
    }

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
        mIsAddAdvertising = isAddAdvertising
        if (mIsAddAdvertising &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_ADVERTISE)) {
            BleServerManager.launchAdvertising(mBleAdapter)
        }
        mGattService = createBleGattService(serviceUuid, serviceType).apply {
            createReadGattCharacteristic(readUuid)
            createWriteGattCharacteristic(writeUuid, describeUuid)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                BleServerManager.addService(mBleGattServer, this)
            }
        }
    }

    private fun createBleGattService(serviceUuid: UUID, serviceType: Int): BluetoothGattService {
        return BleServerManager.getBleGattService(serviceUuid, serviceType)
    }

    private fun createReadGattCharacteristic(readUuid: UUID): BluetoothGattCharacteristic {
        return BleServerManager.createReadGattCharacteristic(mGattService, readUuid)
    }

    private fun createWriteGattCharacteristic(
        writeUuid: UUID,
        describeUuid: UUID?
    ): BluetoothGattCharacteristic {
        return BleServerManager.createWriteGattCharacteristic(mGattService, writeUuid, describeUuid)
    }

    fun close(tag: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_ADVERTISE)) {
            BleServerManager.stopAdvertising(mBleAdapter)
        }
        mGattServerCallbackMap.remove(tag)
        if (mGattServerCallbackMap.isEmpty() &&
            PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            mBleGattServer?.close()
        }
    }
}