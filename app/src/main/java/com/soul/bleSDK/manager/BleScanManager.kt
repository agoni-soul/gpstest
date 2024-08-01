package com.soul.bleSDK.manager

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.soul.SoulApplication
import com.soul.bean.BleScanResult
import com.soul.bean.toBleScanResult
import com.soul.bleSDK.interfaces.IBleScanCallback
import com.soul.log.DOFLogUtil
import com.soul.util.PermissionUtils

object BleScanManager {
    private val TAG = this::class.java.simpleName
    private var mBleManager: BluetoothManager? = null
    private var mBleAdapter: BluetoothAdapter? = null

    init {
        Log.d(TAG, "application = ${SoulApplication.application}")
        mBleManager = SoulApplication.application?.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager?
        mBleAdapter =  mBleManager?.adapter
    }

    fun getBluetoothManager(): BluetoothManager? = mBleManager

    fun getBluetoothAdapter(): BluetoothAdapter? = mBleAdapter

    fun getBondedDevices(): MutableSet<BluetoothDevice>? {
        return if (PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            mBleAdapter?.bondedDevices
        } else {
            null
        }
    }

    fun startDiscovery() {
        Log.d(TAG, "startDiscovery")
        if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_SCAN)) {
            DOFLogUtil.d(TAG, "Manifest.permission.BLUETOOTH_SCAN: PERMISSION_DENIED")
            return
        }
        mBleAdapter?.startDiscovery()
    }

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
    fun enableBle(): Boolean {
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

    fun startScan(bleScanCallback: IBleScanCallback?) {
        if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_SCAN)) {
            DOFLogUtil.d(TAG, "Manifest.permission.BLUETOOTH_SCAN: PERMISSION_DENIED")
            return
        }
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
}