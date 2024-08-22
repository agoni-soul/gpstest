package com.soul.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.soul.base.BaseMvvmFragment
import com.soul.base.BaseViewModel
import com.soul.bean.BleScanResult
import com.soul.bleSDK.interfaces.IBleScanCallback
import com.soul.bleSDK.manager.BleScanManager
import com.soul.bleSDK.scan.BluetoothReceiver
import com.soul.bluetooth.BluetoothActivity.Companion.REQUEST_CODE_BLUETOOTH_DISCOVERABLE
import com.soul.bluetooth.BluetoothActivity.Companion.REQUEST_CODE_PERMISSION
import com.soul.gpstest.R
import com.soul.gpstest.databinding.FragmentBleScanBinding
import com.soul.util.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 *     author : yangzy33
 *     time   : 2024-08-12
 *     desc   :
 *     version: 1.0
 */
class BleScanFragment : BaseMvvmFragment<FragmentBleScanBinding, BaseViewModel>() {

    private var mBluetoothReceiver: BluetoothReceiver? = null
    private var mBleScanReceiverCallback: IBleScanCallback? = null

    private var mBleScanAdapter: BleScanAdapterV2? = null
    private var mBleDevices = mutableListOf<BleScanResult>()

    private var mBleScanCallback: IBleScanCallback? = null

    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_ble_scan

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
        mBleScanAdapter = BleScanAdapterV2(mBleDevices, R.layout.adapter_item_ble_scan).apply {
            setOnItemChildClickListener { _, view, position ->
                when (view.id) {
                    R.id.tv_service_uuids, R.id.tv_device_uuids, R.id.tv_ble_data -> {
                        Log.d(
                            TAG,
                            "convert itemTvBleDataByte: adapterPosition = ${adapterAnimation}, position = $position"
                        )
//                        this.notifyItemChanged(position)
                    }
                }
            }
            setOnItemClickListener { _, _, position ->
                val result = getItem(position)
                Log.d(TAG, "setOnItemClickListener: result =\n$result")
                BleBondManager.createBond(result)
            }
        }
        mViewDataBinding.tvBluetooth.text = "蓝牙扫描"
        mViewDataBinding.rvDeviceBle.let {
            it.adapter = mBleScanAdapter
            val layoutManager = LinearLayoutManager(mContext).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            it.layoutManager = layoutManager
        }
    }

    override fun initData() {
        registerBleReceiver()
//        requestDiscoverable(300)
        mBleScanCallback = object : IBleScanCallback {
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
        }
    }

    private fun registerBleReceiver() {
        mBluetoothReceiver = BluetoothReceiver()
        mBleScanReceiverCallback = object : IBleScanCallback {
            override fun onBatchScanResults(results: MutableList<BleScanResult>?) {

            }

            override fun onScanResult(callbackType: Int, bleScanResult: BleScanResult?) {
                if (bleScanResult?.name?.startsWith("colmo", true) == true ||
                    bleScanResult?.name?.startsWith("midea", true) == true
                ) {
                    return
                }
                if (!bleScanResult?.name.isNullOrBlank()) {
                    if (mBleDevices.find { it.mac == bleScanResult!!.mac } == null) {
                        mBleDevices.add(bleScanResult!!)
                        mBleDevices.sortBy { it.name?.uppercase() }
                        mViewModel.viewModelScope.launch(Dispatchers.Main) {
                            mBleScanAdapter?.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
            }

        }
        mBluetoothReceiver!!.setBleScanCallback(mBleScanReceiverCallback)
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        requireActivity().registerReceiver(mBluetoothReceiver, intentFilter)
    }

    private fun requestDiscoverable(time: Long) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, time)
        }
        startActivityForResult(
            discoverableIntent,
            REQUEST_CODE_BLUETOOTH_DISCOVERABLE
        )
    }

    override fun onResume() {
        super.onResume()
        if (PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_SCAN)) {
            BleScanManager.startScan(mBleScanCallback)
        }
    }

    override fun onPause() {
        super.onPause()
        BleScanManager.stopScan(mBleScanCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(mBluetoothReceiver)
    }
}