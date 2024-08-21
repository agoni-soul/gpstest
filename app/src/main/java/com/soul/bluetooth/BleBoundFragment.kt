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
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.soul.base.BaseMvvmFragment
import com.soul.bean.BleScanResult
import com.soul.bean.toBleScanResult
import com.soul.bleSDK.constants.ScanSettings
import com.soul.bleSDK.interfaces.IBleScanCallback
import com.soul.bleSDK.manager.BleScanManager
import com.soul.bleSDK.scan.BluetoothReceiver
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

    override fun isUsedEncapsulatedPermissions(): Boolean = true

    override fun requestPermissionArray(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
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
        mViewDataBinding.tvBluetooth.apply {
            val tvBluetooth = findViewById<TextView>(R.id.tv_bluetooth)
            tvBluetooth.text = "蓝牙绑定"
            tvBluetooth.setOnClickListener {
                mViewModel.mBleCommunicateManager?.sendMessage("蓝牙图标")
            }
        }
        mViewDataBinding.rvBoundBle.let {
            it.adapter = mBondBleScanAdapter
            val layoutManager = LinearLayoutManager(mContext).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            it.layoutManager = layoutManager
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
        mBluetoothReceiver = BluetoothReceiver().apply {
            setBleBoundCallback(object : IBleScanCallback {
                override fun onBatchScanResults(results: MutableList<BleScanResult>?) {

                }

                override fun onScanResult(callbackType: Int, bleScanResult: BleScanResult?) {
                    bleScanResult ?: return
                    if (callbackType == ScanSettings.CALLBACK_TYPE_REMOVE_BOUND_DEVICE.callbackType) {
                        var i = 0
                        while (i < mBondBleDevices.size) {
                            if (mBondBleDevices[i].mac == bleScanResult.mac) {
                                break
                            } else {
                                i ++
                            }
                        }
                        if (i < mBondBleDevices.size) {
                            mBondBleDevices.removeAt(i)
                            mViewModel.viewModelScope.launch(Dispatchers.Main) {
                                mBondBleScanAdapter?.notifyItemRemoved(i)
                            }
//                            BleScanManager.startDiscovery()
                        }
                    } else {

                        mBondBleDevices.add(bleScanResult)
                        mViewModel.viewModelScope.launch(Dispatchers.Main) {
                            mBondBleScanAdapter?.notifyItemChanged(mBondBleDevices.size)
                        }
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                }

            })
        }
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
}