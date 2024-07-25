package com.soul.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.soul.base.BaseMvvmActivity
import com.soul.bean.BleScanResult
import com.soul.bean.toBleScanResult
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityBluetoothBinding
import com.soul.util.PermissionUtils


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

    private var mBleAdapter: BleAdapter? = null

    private var mBleDevices = mutableListOf<BleScanResult>()

    override fun getViewModelClass(): Class<BleViewModel> = BleViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_bluetooth

    override fun initView() {
        checkSelfPermission(
            mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        )
        mBleAdapter = BleAdapter(mBleDevices).apply {
            setCallback(object: BleAdapter.ItemClickCallback {
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
                    BleHelp.getInstance().setMacAndUuids(result.mac, uuid.toString(), uuid.toString(), uuid.toString())
                    BleHelp.getInstance().start()
                }
            })
        }
        mViewDataBinding?.rvBluetooth?.let {
            it.adapter = mBleAdapter
            val layoutManager = LinearLayoutManager(mContext).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            it.layoutManager = layoutManager
        }
        mViewDataBinding?.tvBluetooth?.text = "蓝牙扫描"
    }

    override fun initData() {
        mViewModel?.startDiscovery()
        BleHelp.getInstance().init(this, object: BleHelp.BleCallback {
            override fun connectSuccess() {
                Log.d(TAG, "connectSuccess")
            }

            override fun getDeviceReturnData(data: ByteArray?) {
                Log.d(TAG, "getDeviceReturnData: data = ${data?.toString()}")
            }

            override fun error(e: Int) {
                Log.e(TAG, "error: e = $e")
            }
        })
        if (PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_SCAN)) {
            mViewModel?.bluetoothAdapter?.bluetoothLeScanner?.startScan(object : ScanCallback() {
                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    super.onBatchScanResults(results)
                    results ?: return
                    for (result in results) {
//                        Log.d(TAG, "onBatchScanResults: device: ${result.device?.name}")
                    }
                }

                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
//                    Log.d(TAG, "onScanResult: device: ${result?.device?.name}")
                    result?.device?.let {
                        val bleScanResult = result.toBleScanResult()
                        if (!it.name.isNullOrBlank() && !it.address.isNullOrBlank() &&
                            !mBleDevices.contains(bleScanResult)
                        ) {
                            mBleDevices.add(bleScanResult)
                            mBleAdapter?.notifyItemChanged(mBleDevices.size)
                        }
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
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
        mViewModel?.stopDiscovery()
    }
}