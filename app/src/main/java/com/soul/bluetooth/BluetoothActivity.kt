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

    private var mBluetoothReceiver: BluetoothReceiver? = null

    private var mBleAdapter: BleAdapter? = null

    private var mBleDevices = mutableListOf<BluetoothDevice>()

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
        mBleAdapter = BleAdapter(mBleDevices)
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
        mBluetoothReceiver = BluetoothReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mBluetoothReceiver, intentFilter)
        mViewModel?.startDiscovery()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_SCAN)) {
            mViewModel?.bluetoothAdapter?.bluetoothLeScanner?.startScan(object : ScanCallback() {
                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    super.onBatchScanResults(results)
                    results ?: return
                    for (result in results) {
                        Log.d(TAG, "startScan: device: ${result.device?.name}")
                    }
                }

                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    Log.d(TAG, "startScan: device: ${result?.device?.name}")
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    Log.i(TAG, "startScan: errorCode: $errorCode")
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

    inner class BluetoothReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context ?: return
            intent ?: return
            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!it.name.isNullOrBlank() && !it.address.isNullOrBlank() &&
                        !mBleDevices.contains(it)
                    ) {
                        mBleDevices.add(it)
                        mBleAdapter?.notifyItemChanged(mBleDevices.size)
                    }
                }
            }
        }
    }
}