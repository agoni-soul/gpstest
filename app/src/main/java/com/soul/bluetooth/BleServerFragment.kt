package com.soul.bluetooth

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.os.Build
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.soul.base.BaseMvvmFragment
import com.soul.base.BaseViewModel
import com.soul.bleSDK.commication.BleServerManager
import com.soul.bleSDK.constants.BleBlueImpl
import com.soul.bleSDK.manager.BleScanManager
import com.soul.gpstest.R
import com.soul.gpstest.databinding.FragmentBleServerBinding
import com.soul.util.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 *     author : yangzy33
 *     time   : 2024-08-21
 *     desc   :
 *     version: 1.0
 */
class BleServerFragment : BaseMvvmFragment<FragmentBleServerBinding, BaseViewModel>() {

    private val mSb = StringBuilder()
    private var mBluetoothGattServer: BluetoothGattServer? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    @SuppressWarnings("missingPermission")
    private val gattServiceCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            device ?: return
            Log.d(TAG, "zsr onConnectionStateChange: ")
            if (status == BluetoothGatt.GATT_SUCCESS && newState == 2) {
                logInfo("连接到中心设备: ${device?.name}")
            } else {
                logInfo("与: ${device?.name} 断开连接失败！")
            }
        }


        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

            /**
             * 中心设备read时，回调
             */
            val data = "this is a test from ble server"
            mBluetoothGattServer?.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS,
                offset, data.toByteArray()
            )
            logInfo("客户端读取 [characteristic ${characteristic?.uuid}] $data")
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
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            mBluetoothGattServer?.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS,
                offset, value
            )
            value?.let {
                logInfo("客户端写入 [characteristic ${characteristic?.uuid}] ${String(it)}")
            }
        }

        override fun onDescriptorReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            descriptor: BluetoothGattDescriptor?
        ) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)
            val data = "this is a test"
            mBluetoothGattServer?.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS,
                offset, data.toByteArray()
            )
            logInfo("客户端读取 [descriptor ${descriptor?.uuid}] $data")
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
            super.onDescriptorWriteRequest(
                device,
                requestId,
                descriptor,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )

            value?.let {
                logInfo("客户端写入 [descriptor ${descriptor?.uuid}] ${String(it)}")
                // 简单模拟通知客户端Characteristic变化
                Log.d(TAG, "zsr onDescriptorWriteRequest: $value")
            }


        }

        override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
            super.onExecuteWrite(device, requestId, execute)
            Log.d(TAG, "zsr onExecuteWrite: ")
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)
            Log.d(TAG, "zsr onNotificationSent: ")
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            super.onMtuChanged(device, mtu)
            Log.d(TAG, "zsr onMtuChanged: ")
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            logInfo("服务准备就绪，请搜索广播")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
                logInfo("广播数据超过31个字节了 !")
            } else {
                logInfo("服务启动失败: $errorCode")
            }
        }
    }

    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_ble_server

    override fun isUsedEncapsulatedPermissions(): Boolean {
        return true
    }

    override fun requestPermissionArray(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
    }

    override fun handlePermissionResult(permissionResultMap: Map<String, Boolean>) {
        permissionResultMap.forEach { (k, v) ->
            Log.d(TAG, "$k ----->>>>>  $v")
        }
    }

    override fun initView() {
    }

    override fun initData() {
        initBle()
    }

    private fun initBle() {
        if (!PermissionUtils.checkMultiPermission(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        ) return
        bluetoothAdapter = BleScanManager.getBluetoothAdapter()
        bluetoothAdapter?.name = "k20"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            BleServerManager.launchAdvertising(bluetoothAdapter)
        }
        BleServerManager.getBleGattService(
            BleBlueImpl.UUID_SERVICE,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        ).apply {
            BleServerManager.createReadGattCharacteristic(this, BleBlueImpl.UUID_READ_NOTIFY)
            BleServerManager.createWriteGattCharacteristic(
                this,
                BleBlueImpl.UUID_WRITE,
                BleBlueImpl.UUID_DESCRIBE
            )
            val bluetoothManager = BleScanManager.getBluetoothManager()
            //打开 GATT 服务，方便客户端连接
            mBluetoothGattServer =
                bluetoothManager?.openGattServer(requireContext(), gattServiceCallback)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                BleServerManager.addService(mBluetoothGattServer, this)
            }
        }
    }

    private fun logInfo(msg: String) {
        Log.d(TAG, "logInfo = ${mSb.apply { append(msg).append("\n") }}")
        mViewModel.viewModelScope.launch(Dispatchers.Main) {
            mViewDataBinding.info.text = mSb.toString()
        }
    }

    @SuppressWarnings("missingPermission")
    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            BleServerManager.stopAdvertising(bluetoothAdapter)
        }
        mBluetoothGattServer?.close()
    }
}