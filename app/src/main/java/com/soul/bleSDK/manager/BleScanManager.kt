package com.soul.bleSDK.manager

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.util.Log
import com.blankj.utilcode.util.NotificationUtils.Importance
import com.soul.SoulApplication
import com.soul.bean.BleScanResult
import com.soul.bean.toBleScanResult
import com.soul.bleSDK.interfaces.IBleScanCallback
import com.soul.log.DOFLogUtil
import com.soul.util.PermissionUtils

object BleScanManager {
    const val REQUEST_ENABLE_BLE = 1000
    private val TAG = javaClass.simpleName
    private var mBleManager: BluetoothManager? = null
    private var mBleAdapter: BluetoothAdapter? = null
    private var mIsScanning = false

    init {
        Log.d(TAG, "application = ${SoulApplication.application}")
        mBleManager = SoulApplication.application?.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager?
        mBleAdapter =  mBleManager?.adapter
    }

    fun isScanning(): Boolean = mIsScanning

    fun getBluetoothManager(): BluetoothManager? = mBleManager

    fun getBluetoothAdapter(): BluetoothAdapter? = mBleAdapter

    fun getBondedDevices(): MutableSet<BluetoothDevice>? {
        return if (PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            mBleAdapter?.bondedDevices
        } else {
            null
        }
    }

    /**
     * 推荐使用 [startScan()]
     */
    @Deprecated("recommend to use startScan()", ReplaceWith("startScan(IBleScanCallback)"))
    fun startDiscovery() {
        Log.d(TAG, "startDiscovery")
        if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_SCAN)) {
            DOFLogUtil.d(TAG, "Manifest.permission.BLUETOOTH_SCAN: PERMISSION_DENIED")
            return
        }
        mBleAdapter?.startDiscovery()
    }


    /**
     * 推荐使用 [stopScan()]
     */
    @Deprecated("recommend to use stopScan()", ReplaceWith("stopScan(IBleScanCallback)"))
    fun cancelDiscovery() {
        Log.d(TAG, "cancelDiscovery")
        if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_SCAN)) {
            DOFLogUtil.d(TAG, "Manifest.permission.BLUETOOTH_SCAN: PERMISSION_DENIED")
            return
        }
        mBleAdapter?.cancelDiscovery()
    }

    /**
     * 打开手机蓝牙
     *
     * @return true 表示打开成功
     */
    fun isEnableBle(): Boolean {
        if (mBleAdapter?.isEnabled == false) {
            if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                DOFLogUtil.d(TAG, "Manifest.permission.BLUETOOTH_CONNECT: PERMISSION_DENIED")
                return false
            }
            //若未打开手机蓝牙，则会弹出一个系统的是否打开/关闭蓝牙的对话框，禁止或者未处理返回false，允许返回true
            //若已打开手机蓝牙，直接返回true
            val enableState: Boolean = mBleAdapter!!.enable()
            Log.d(TAG, "（用户操作）手机蓝牙是否打开成功：$enableState")
            return enableState
        } else {
            return true
        }
    }

    fun requestBluetoothPermission(activity: Activity?) {
        if (!isEnableBle()) {
            val enableBleIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                activity?.startActivityForResult(enableBleIntent, REQUEST_ENABLE_BLE)
            }
        }
    }

    fun startScan(bleScanCallback: IBleScanCallback?) {
        if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_SCAN)) {
            DOFLogUtil.d(TAG, "Manifest.permission.BLUETOOTH_SCAN: PERMISSION_DENIED")
            mIsScanning = false
            return
        }
        mIsScanning = true
        mBleAdapter?.bluetoothLeScanner?.startScan(object: ScanCallback() {
            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                val mutableList = mutableListOf<BleScanResult>()
                results?.forEach {
                    val bleScanResult = it.toBleScanResult()
                    mutableList.add(bleScanResult)
                }
                bleScanCallback?.onBatchScanResults(mutableList)
            }

            override fun onScanResult(callbackType: Int, bleScanResult: ScanResult?) {
                bleScanCallback?.onScanResult(callbackType, bleScanResult?.toBleScanResult())
            }

            override fun onScanFailed(errorCode: Int) {
                bleScanCallback?.onScanFailed(errorCode)
            }
        })
    }

    fun stopScan(bleScanCallback: IBleScanCallback?) {
        mIsScanning = false
        if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_SCAN)) {
            DOFLogUtil.d(TAG, "Manifest.permission.BLUETOOTH_SCAN: PERMISSION_DENIED")
            return
        }
        mBleAdapter?.bluetoothLeScanner?.stopScan(object: ScanCallback() {
            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                val mutableList = mutableListOf<BleScanResult>()
                results?.forEach {
                    val bleScanResult = it.toBleScanResult()
                    mutableList.add(bleScanResult)
                }
                bleScanCallback?.onBatchScanResults(mutableList)
            }

            override fun onScanResult(callbackType: Int, bleScanResult: ScanResult?) {
                bleScanCallback?.onScanResult(callbackType, bleScanResult?.toBleScanResult())
            }

            override fun onScanFailed(errorCode: Int) {
                bleScanCallback?.onScanFailed(errorCode)
            }
        })
    }

    fun stopScan() {
        stopScan(null)
    }
}