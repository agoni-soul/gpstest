package com.soul.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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
import com.soul.bleSDK.interfaces.IBleScanCallback
import com.soul.bleSDK.manager.BleScanManager
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
        private const val REQUEST_CODE_PERMISSION = 101
        private const val REQUEST_CODE_BLUETOOTH_DISCOVERABLE = 102
    }

    private var mBluetoothReceiver: BluetoothReceiver? = null

    private var mBleScanAdapter: BleScanAdapter? = null
    private var mBleDevices = mutableListOf<BleScanResult>()

    private var mBondBleScanAdapter: BleBondedAdapter? = null
    private var mBondBleDevices = mutableListOf<BleScanResult>()

    override fun getViewModelClass(): Class<BleViewModel> = BleViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_bluetooth

    override fun initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkSelfPermission(mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            ))
        } else {
            checkSelfPermission(mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            ))
        }
        mBleScanAdapter = BleScanAdapter(mBleDevices).apply {
            setCallback(object : BleScanAdapter.ItemClickCallback {
                override fun onClick(result: BleScanResult) {
                    BleBondManager.createBond(result)
                }
            })
        }
        mViewDataBinding.rvDeviceBle.let {
            it.adapter = mBleScanAdapter
            val layoutManager = LinearLayoutManager(mContext).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            it.layoutManager = layoutManager
        }
        mViewDataBinding.tvBluetooth.text = "蓝牙扫描"
        mViewDataBinding.tvBluetooth.setOnClickListener {
            mViewModel.mBleCommunicateManager?.sendMessage("蓝牙图标")
        }

        mBondBleScanAdapter = BleBondedAdapter(mBondBleDevices, R.layout.adapter_item_ble_bonded).apply {
            setCallback(object : BleBondedAdapter.ItemClickCallback {

                override fun onClickUnbind(result: BleScanResult) {
                    BleBondManager.removeBond(result)
                }

                override fun onClickCommunicate(result: BleScanResult) {
                    mViewModel.mBleRfcommConnectManager?.connect(result)
                }
            })
        }
        mViewDataBinding.rvBondBle.let {
            it.adapter = mBondBleScanAdapter
            val layoutManager = LinearLayoutManager(mContext).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            it.layoutManager = layoutManager
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
                REQUEST_CODE_PERMISSION
            )
        }
    }

    override fun initData() {
        BleScanManager.startDiscovery()
        registerBleReceiver()
        requestDiscoverable(300)
        if (PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_SCAN)) {
            BleScanManager.startScan(object: IBleScanCallback {
                override fun onBatchScanResults(results: MutableList<BleScanResult>?) {
                    results ?: return
                    results.toString()
                }

                override fun onScanResult(callbackType: Int, bleScanResult: BleScanResult?) {
                    bleScanResult?.let { result ->
                        if (result.name?.startsWith("colmo", true) == true ||
                            result.name?.startsWith("midea", true) == true
                        ) {
                            return@let
                        }
                        if (!result.name.isNullOrBlank() && !result.mac.isNullOrBlank()) {
                            if (mBleDevices.find { it.mac == result.mac } == null) {
                                mBleDevices.add(result)
                                mBleDevices.sortBy { it.name?.uppercase() }
                                mBleScanAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    Log.i(TAG, "onScanFailed: errorCode: $errorCode")
                }
            })
        }
        BleScanManager.getBondedDevices()?.let {
            for (device in it) {
                mBondBleDevices.add(device.toBleScanResult())
            }
        }
    }

    private fun registerBleReceiver() {
        mBluetoothReceiver = BluetoothReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(mBluetoothReceiver, intentFilter)
    }

    private fun requestDiscoverable(time: Long) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, time)
        }
        startActivityForResult(discoverableIntent, REQUEST_CODE_BLUETOOTH_DISCOVERABLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_BLUETOOTH_DISCOVERABLE) {
            Log.d(TAG, "resultCode = ${resultCode}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION -> {
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
        BleScanManager.cancelDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBluetoothReceiver)
        mViewModel.close()
    }

    inner class BluetoothReceiver : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
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
                if (bleDevice?.name?.startsWith("colmo", true) == true ||
                    bleDevice?.name?.startsWith("midea", true) == true) {
                    return
                }
                val bleScanResult = bleDevice?.toBleScanResult()
                if (!bleScanResult?.name.isNullOrBlank() && !bleDevice?.address.isNullOrBlank()) {
                    if (mBleDevices.find { it.mac == bleDevice!!.address } == null) {
                        mBleDevices.add(bleScanResult!!)
                        mBleDevices.sortBy { it.name?.uppercase() }
                        mBleScanAdapter?.notifyDataSetChanged()
                    }
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == intent.action) {
                mViewModel.viewModelScope.launch(Dispatchers.IO) {
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                    val preState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)
                    val bleDevice: BluetoothDevice =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        } ?: return@launch
                    Log.d(TAG, "BluetoothReceiver: state = $state, preState = $preState, device.name = ${bleDevice.name}, device.mac = ${bleDevice.address}")
                    val bondedDevices = BleScanManager.getBondedDevices() ?: return@launch
                    if (state == BluetoothDevice.BOND_BONDED &&
                        (preState == BluetoothDevice.BOND_NONE || preState == BluetoothDevice.BOND_BONDING)) {
                        if (bondedDevices.find { it.address == bleDevice.address } != null) {
                            mBondBleDevices.add(bleDevice.toBleScanResult())
                            withContext(Dispatchers.Main) {
                                mBondBleScanAdapter?.notifyItemChanged(mBondBleDevices.size)
                            }
                            BleScanManager.startDiscovery()
                        }
                    } else if (state == BluetoothDevice.BOND_NONE &&
                        (preState == BluetoothDevice.BOND_BONDED || preState == BluetoothDevice.BOND_BONDING)) {
                        var i = 0
                        while (i < mBondBleDevices.size) {
                            if (mBondBleDevices[i].mac == bleDevice.address) {
                                break
                            } else {
                                i ++
                            }
                        }
                        if (i < mBondBleDevices.size) {
                            mBondBleDevices.removeAt(i)
                            withContext(Dispatchers.Main) {
                                mBondBleScanAdapter?.notifyItemRemoved(i)
                            }
                            BleScanManager.startDiscovery()
                        }
                    }
                }
            }
        }
    }
}