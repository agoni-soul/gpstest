package com.soul.bluetooth

import android.Manifest
import android.bluetooth.*
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.lifecycle.viewModelScope
import com.soul.base.BaseMvvmFragment
import com.soul.base.BaseViewModel
import com.soul.bleSDK.commication.BleServerImpl
import com.soul.bleSDK.commication.BleServerManager
import com.soul.bleSDK.constants.BleBlueImpl
import com.soul.bleSDK.manager.BleScanManager
import com.soul.gpstest.R
import com.soul.gpstest.databinding.FragmentBleServerBinding
import com.soul.log.DOFLogUtil
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
            device ?: return
            DOFLogUtil.d(TAG, "zsr onConnectionStateChange: ")
            if (status == BluetoothGatt.GATT_SUCCESS && newState == 2) {
                logInfo("连接到中心设备: ${device.name}")
            } else {
                logInfo("与: ${device.name} 断开连接失败！")
            }
        }


        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
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
            value?.let {
                logInfo("客户端写入 [descriptor ${descriptor?.uuid}] ${String(it)}")
                // 简单模拟通知客户端Characteristic变化
                DOFLogUtil.d(TAG, "zsr onDescriptorWriteRequest: $value")
            }
        }

        override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
            DOFLogUtil.d(TAG, "zsr onExecuteWrite: ")
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            DOFLogUtil.d(TAG, "zsr onNotificationSent: ")
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            DOFLogUtil.d(TAG, "zsr onMtuChanged: ")
        }
    }

    private var mBleServerImpl: BleServerImpl? = null

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
            DOFLogUtil.d(TAG, "$k ----->>>>>  $v")
        }
    }

    override fun initView() {
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun initData() {
        bluetoothAdapter = BleScanManager.getBluetoothAdapter()
        bluetoothAdapter?.name = "k20"
        val bleManager = BleScanManager.getBluetoothManager()
        if (bluetoothAdapter != null && bleManager != null) {
            mBleServerImpl = BleServerImpl(requireContext(), bluetoothAdapter!!, bleManager)
        }
        mBleServerImpl?.apply {
            setGattServiceCallback(TAG, gattServiceCallback)
            createAndAddBleService(
                true,
                BleBlueImpl.UUID_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY,
                BleBlueImpl.UUID_READ_NOTIFY,
                BleBlueImpl.UUID_WRITE,
                BleBlueImpl.UUID_DESCRIBE
            )
        }
    }

    private fun logInfo(msg: String) {
        DOFLogUtil.d(TAG, "logInfo = ${mSb.apply { append(msg).append("\n") }}")
        mViewModel.viewModelScope.launch(Dispatchers.Main) {
            mViewDataBinding.info.text = mSb.toString()
        }
    }

    @SuppressWarnings("missingPermission")
    override fun onDestroy() {
        super.onDestroy()
        mBleServerImpl?.close(TAG)
    }
}