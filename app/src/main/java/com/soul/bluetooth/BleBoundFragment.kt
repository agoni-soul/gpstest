package com.soul.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import com.soul.base.BaseMvvmFragment
import com.soul.bean.BleScanResult
import com.soul.bean.toBleScanResult
import com.soul.bleSDK.manager.BleScanManager
import com.soul.bluetooth.BluetoothActivity.Companion.REQUEST_CODE_PERMISSION
import com.soul.gpstest.R
import com.soul.gpstest.databinding.FragmentBleBoundBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 *     author : yangzy33
 *     time   : 2024-08-13
 *     desc   :
 *     version: 1.0
 */
class BleBoundFragment: BaseMvvmFragment<FragmentBleBoundBinding, BleViewModel>() {

    private var mBluetoothReceiver: BluetoothReceiver? = null

    private var mBondBleScanAdapter: BleBondedAdapter? = null
    private var mBondBleDevices = mutableListOf<BleScanResult>()

    override fun getViewModelClass(): Class<BleViewModel> = BleViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_ble_bound

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
        mViewDataBinding.rvBoundBle.let {
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
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)) {
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
        registerBleReceiver()
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
        requireActivity().registerReceiver(mBluetoothReceiver, intentFilter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(mBluetoothReceiver)
        mViewModel.close()
    }

    inner class BluetoothReceiver : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            context ?: return
            intent ?: return
            intent.action ?: return
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == intent.action) {
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