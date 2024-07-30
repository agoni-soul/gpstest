package com.soul.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.soul.base.BaseMvvmActivity
import com.soul.bean.BleScanResult
import com.soul.bean.toBleScanResult
import com.soul.bleSDK.BleConnectManager
import com.soul.bleSDK.BleListener
import com.soul.bleSDK.BleSDKManager
import com.soul.bleSDK.BleScanManager
import com.soul.bleSDK.interfaces.BaseBleListener
import com.soul.bleSDK.interfaces.IBleScanCallback
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityBluetoothBinding
import com.soul.util.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 *     author : yangzy33
 *     time   : 2024-07-04
 *     desc   :
 *     version: 1.0
 */
class BluetoothActivity : BaseMvvmActivity<ActivityBluetoothBinding, BleViewModel>() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
    }

    private var mBluetoothReceiver: BluetoothReceiver? = null

    private var mBleAdapter: BleAdapter? = null
    private var mBleDevices = mutableListOf<BleScanResult>()

    private var mBondBleAdapter: BleAdapter? = null
    private var mBondBleDevices = mutableListOf<BleScanResult>()

    override fun getViewModelClass(): Class<BleViewModel> = BleViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_bluetooth

    override fun initView() {
        checkSelfPermission(mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            ))
        mBleAdapter = BleAdapter(mBleDevices).apply {
            setCallback(object : BleAdapter.ItemClickCallback {
                override fun onClick(result: BleScanResult) {
                    Log.d(TAG, "onClick: \ndevice = ${result.device}")
                    var parcelUuid = result.scanRecord?.serviceUuids?.get(0)
                    val serviceData = result.scanRecord?.serviceData
                    Log.d(TAG, "onClick: \nserviceData = $serviceData")
                    if (parcelUuid == null && !serviceData.isNullOrEmpty()) {
                        for (key in serviceData.keys) {
                            if (key != null) {
                                parcelUuid = key
                                break
                            }
                        }
                    }
                    val uuid = parcelUuid?.uuid
                    Log.d(TAG, "onClick: \nuuid = ${uuid}")
                    val serviceDataSingle = serviceData?.get(parcelUuid)
                    Log.d(TAG, "onClick: \nserviceDataSingle = $serviceDataSingle")
                    mViewModel.mBleConnectManager?.connect(result)
                }
            })
        }
        mViewDataBinding.rvDeviceBle.let {
            it.adapter = mBleAdapter
            val layoutManager = LinearLayoutManager(mContext).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            it.layoutManager = layoutManager
        }
        mViewDataBinding.tvBluetooth.text = "蓝牙扫描"
        mViewDataBinding.tvBluetooth.setOnClickListener {
            mViewModel.mBleSDKManager?.sendMsg("蓝牙图标")
        }

        mViewModel.mBleConnectManager?.getBluetoothAdapter()?.bondedDevices?.let {
            for (device in it) {
                mBondBleDevices.add(device.toBleScanResult())
            }
        }
        mBondBleAdapter = BleAdapter(mBondBleDevices).apply {
            setCallback(object : BleAdapter.ItemClickCallback {
                override fun onClick(result: BleScanResult) {
                    mViewModel.mBleSDKManager?.start(result.device)
                }
            })
        }
        mViewDataBinding.rvBondBle.let {
            it.adapter = mBondBleAdapter
            val layoutManager = LinearLayoutManager(mContext).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            it.layoutManager = layoutManager
        }
    }

    override fun initData() {
        mViewModel.mBleConnectManager?.startDiscovery()
        registerBleReceiver()
        if (PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_SCAN)) {
            mViewModel.mBleConnectManager?.startScan(object: IBleScanCallback {
                override fun onBatchScanResults(results: MutableList<BleScanResult>?) {
                    results ?: return
                }

                override fun onScanResult(callbackType: Int, result: BleScanResult?) {
                    result?.device?.let { bleDevice ->
                        if (bleDevice.name?.startsWith("colmo", true) == true ||
                            bleDevice.name?.startsWith("midea", true) == true
                        ) {
                            return@let
                        }
                        if (!bleDevice.name.isNullOrBlank() && !bleDevice.address.isNullOrBlank()) {
                            if (mBleDevices.find { it.mac == bleDevice.address } == null) {
                                mBleDevices.add(result)
                                mBleDevices.sortBy { it.name?.uppercase() }
                                mBleAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    Log.i(TAG, "onScanFailed: errorCode: $errorCode")
                }
            })
        }
    }

    private fun checkSelfPermission(permissions: MutableList<String>) {
        val denyPermissionList = mutableListOf<String>()
        for (permission in permissions) {
            val permissionValue =
                ActivityCompat.checkSelfPermission(mContext as Activity, permission)
            if (permissionValue == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "checkSelfPermission: checkSelfPermission, permission = $permission")
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Log.d(
                    TAG,
                    "checkSelfPermission: shouldShowRequestPermissionRationale, permission = $permission"
                )
            } else {
                denyPermissionList.add(permission)
                Log.d(TAG, "checkSelfPermission: $permission")
            }
        }
        if (denyPermissionList.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                mContext as Activity,
                denyPermissionList.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun registerBleReceiver() {
        mBluetoothReceiver = BluetoothReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(mBluetoothReceiver, intentFilter)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                for (i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d(
                            TAG,
                            "onRequestPermissionsResult: permission = ${permissions[i]}, grantValue = ${grantResults[i]}"
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mViewModel.mBleConnectManager?.cancelDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBluetoothReceiver)
        mViewModel.mBleConnectManager?.close()
        mViewModel.mBleSDKManager?.close()
    }

    inner class BluetoothReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context ?: return
            intent ?: return
            intent.action ?: return
            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val bleDevice: BluetoothDevice? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )
                    } else {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                if (bleDevice?.name?.startsWith(
                        "colmo",
                        true
                    ) == true || bleDevice?.name?.startsWith("midea", true) == true
                ) {
                    return
                }
                val bleScanResult = bleDevice?.toBleScanResult()
                if (!bleDevice?.name.isNullOrBlank() && !bleDevice?.address.isNullOrBlank()) {
                    if (mBleDevices.find { it.mac == bleDevice!!.address } == null) {
                        mBleDevices.add(bleScanResult!!)
                        mBleDevices.sortBy { it.name?.uppercase() }
                        mBleAdapter?.notifyDataSetChanged()
                    }
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == intent.action) {
                mViewModel.viewModelScope.launch(Dispatchers.IO) {
                    mViewModel.mBleConnectManager?.getBluetoothAdapter()?.bondedDevices?.toMutableList()?.let {
                        val bleDevice: BluetoothDevice =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                intent.getParcelableExtra(
                                    BluetoothDevice.EXTRA_DEVICE,
                                    BluetoothDevice::class.java
                                )
                            } else {
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                            } ?: return@launch
                        var isUpdate = false
                        val result = bleDevice.toBleScanResult()
                        if (result in mBleDevices) {
                            mBleDevices.find { bleScanResult ->
                                result == bleScanResult
                            }?.apply {
                                bondState = result.bondState
                                device = result.device
                            }
                            isUpdate = true
                        }

                        mBondBleDevices.clear()
                        it.forEach { device ->
                            mBondBleDevices.add(device.toBleScanResult())
                        }
                        withContext(Dispatchers.Main) {
                            mBondBleAdapter?.notifyDataSetChanged()
                            if (isUpdate) {
                                mBleAdapter?.notifyDataSetChanged()
                            }
                        }
                        // TODO 逻辑有问题，后续修复
//                        Log.d(TAG, "BluetoothReceiver#onReceive: bondedDevices = $it")
//                        var isUpdate = false
//                        for (device in it) {
//                            val newResult = device.toBleScanResult()
//                            var isRemove = false
//                            var isEqual = false
//                            val iter = mBondBleDevices.iterator()
//                            while (iter.hasNext()) {
//                                val result = iter.next()
//                                if (result == newResult) {
//                                    isEqual = true
//                                    if (newResult.bondState != BluetoothDevice.BOND_BONDED) {
//                                        mBondBleDevices.remove(result)
//                                        isRemove = true
//                                    }
//                                }
//                            }
//                            if (isRemove) {
//                                isUpdate = true
//                            } else if (!isEqual) {
//                                mBondBleDevices.add(newResult)
//                                isUpdate = true
//                            }
//                        }
//                        if (isUpdate) {
//                            withContext(Dispatchers.Main) {
//                                mBondBleAdapter?.notifyDataSetChanged()
//                            }
//                        }
                    }
                }
            }
        }
    }
}