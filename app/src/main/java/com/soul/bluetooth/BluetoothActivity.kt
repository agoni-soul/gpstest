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
import com.soul.bleSDK.BleListener
import com.soul.bleSDK.BleSDKManager
import com.soul.bleSDK.interfaces.BaseBleListener
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

    private var socket: BluetoothSocket? = null

    private var mResult: BleScanResult? = null

    private var mBleManager: BleSDKManager? = null
    private val mReadListener: BleListener by lazy {
        object : BleListener {
            override fun onStart() {
                Log.d(TAG, "onStart: 正在连接...")
            }

            override fun onReceiveData(socket: BluetoothSocket?, msg: String) {
                Log.d(TAG, "onReceiveData: ${socket?.remoteDevice?.name + ": " + msg}")
            }

            override fun onConnected(msg: String) {
                super.onConnected(msg)
                Log.d(TAG, "onConnected: 已连接")
            }

            override fun onFail(error: String) {
                Log.d(TAG, "onFail: 已配对 error = $error")
            }
        }
    }
    private val mWriteListener: BaseBleListener by lazy {
        object : BaseBleListener {
            override fun onSendMsg(socket: BluetoothSocket?, msg: String) {
                Log.d(TAG, "onSendMsg: 我: $msg")
            }

            override fun onFail(error: String) {
                Log.d(TAG, "write onFail: $error")
            }
        }
    }

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
                    mResult = result
                    mViewModel.viewModelScope.launch(Dispatchers.IO) {
                        val retryAmount = 10
                        var retryCount = 0
                        while (retryCount <= retryAmount) {
                            try {
                                if (result.bondState != BluetoothDevice.BOND_BONDED) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val createSocket = BluetoothDevice::class.java.getMethod(
                                            "createRfcommSocket",
                                            Int::class.java
                                        )
                                        createSocket.isAccessible = true

                                        //找一个通道去连接即可，channel 1～30
                                        socket =
                                            createSocket.invoke(result.device, 1) as BluetoothSocket
                                        //阻塞等待
                                        socket?.connect()
                                        //延时，以便于去连接
                                        Thread.sleep(2000)
                                    } else {
                                        break
                                    }
                                }

                                if (result.device != null && connectA2dp(result.device)) {
                                    Log.d(TAG, "initView: connect success")
                                } else {
                                    Log.d(TAG, "initView: connect fail")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "initView: connect Error: msg = $e")
                            }
                            retryCount++
                        }
                    }
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
            mBleManager?.sendMsg("蓝牙图标")
        }

        mViewModel.bluetoothAdapter?.bondedDevices?.let {
            for (device in it) {
                mBondBleDevices.add(device.toBleScanResult())
            }
        }
        mBondBleAdapter = BleAdapter(mBondBleDevices).apply {
            setCallback(object : BleAdapter.ItemClickCallback {
                override fun onClick(result: BleScanResult) {
                    mBleManager = BleSDKManager().apply {
                        setReadListener(mReadListener)
                        setWriteListener(mWriteListener)
                        start(result.device)
                    }
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

    private fun connectA2dp(device: BluetoothDevice?): Boolean {
        device ?: return false
        //连接 a2dp
        val connect =
            BluetoothA2dp::class.java.getMethod("connect", BluetoothDevice::class.java)
        connect.isAccessible = true
        return connect.invoke(mViewModel.bleA2dp, device) as Boolean
    }

    override fun initData() {
        mViewModel.startDiscovery()
        mBluetoothReceiver = BluetoothReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(mBluetoothReceiver, intentFilter)
        BleHelp.getInstance().init(this, object : BleHelp.BleCallback {
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
            mViewModel.bluetoothAdapter?.bluetoothLeScanner?.startScan(object : ScanCallback() {
                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    super.onBatchScanResults(results)
                    results ?: return
                    for (result in results) {
//                        Log.d(TAG, "onBatchScanResults: device: ${result.device?.name}")
                    }
                }

                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    result?.device?.let { bleDevice ->
                        if (bleDevice.name?.startsWith(
                                "colmo",
                                true
                            ) == true || bleDevice.name?.startsWith("midea", true) == true
                        ) {
                            return@let
                        }
                        val bleScanResult = result.toBleScanResult()
                        if (!bleDevice.name.isNullOrBlank() && !bleDevice.address.isNullOrBlank()) {
                            if (mBleDevices.find { it.mac == bleDevice.address } == null) {
                                mBleDevices.add(bleScanResult)
                                mBleDevices.sortBy { it.name?.uppercase() }
                                mBleAdapter?.notifyDataSetChanged()
                            }
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
        mViewModel.stopDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBluetoothReceiver)
        try {
            socket?.close()

            try {
                //通过反射获取BluetoothA2dp中connect方法（hide的），断开连接。
                val connectMethod = BluetoothA2dp::class.java.getMethod(
                    "disconnect",
                    BluetoothDevice::class.java
                )
                connectMethod.invoke(mViewModel.bleA2dp, mResult?.device)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mBleManager?.close()
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
                    mViewModel.bluetoothAdapter?.bondedDevices?.toMutableList()?.let {
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