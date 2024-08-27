package com.soul.bleSDK.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.util.Log
import com.soul.bean.BleScanResult
import com.soul.bean.toBleScanResult
import com.soul.bleSDK.constants.toScanSettings
import com.soul.bleSDK.interfaces.IBleScanCallback
import com.soul.bleSDK.permissions.BleSDkPermissionManager
import com.soul.log.DOFLogUtil
import kotlin.math.tan

class BleScanManager private constructor(): BaseBleManager() {

    companion object {
        const val REQUEST_ENABLE_BLE = 1000

        @Volatile
        private var mInstance: BleScanManager? = null

        fun getInstance(): BleScanManager? {
            return if (BleSDkPermissionManager.isGrantScanAllPermissions()) {
                mInstance ?: synchronized(this) {
                    mInstance ?: BleScanManager().also { mInstance = it }
                }
            } else {
                null
            }
        }
    }
    private var mIsScanning = false
    private val mBleScanCallbackMap = mutableMapOf<String, IBleScanCallback?>()
    private val mScanningMap = mutableMapOf<String, Boolean>()
    private var mScanCallback: ScanCallback? = null

    fun isScanning(): Boolean = mIsScanning

    fun isSubScanning(tag: String?): Boolean = mScanningMap[tag] ?: false

    @SuppressLint("MissingPermission")
    fun getBondedDevices(): MutableSet<BluetoothDevice>? {
        return if (BleSDkPermissionManager.isGrantConnectRelatedPermissions()) {
            mBleAdapter?.bondedDevices
        } else {
            null
        }
    }

    /**
     * 经典蓝牙扫码
     */
//    @Deprecated("recommend to use startScan()", ReplaceWith("startScan(IBleScanCallback)"))
    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        Log.d(TAG, "startDiscovery")
        if (!BleSDkPermissionManager.isGrantScanAllPermissions()) {
            return
        }
        mBleAdapter?.startDiscovery()
    }

    /**
     * 经典蓝牙扫码
     */
//    @Deprecated("recommend to use stopScan()", ReplaceWith("stopScan(IBleScanCallback)"))
    @SuppressLint("MissingPermission")
    fun cancelDiscovery() {
        Log.d(TAG, "cancelDiscovery")
        if (!BleSDkPermissionManager.isGrantScanAllPermissions()) {
            return
        }
        mBleAdapter?.cancelDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun requestBluetoothPermission(activity: Activity?) {
        if (!BleSDkPermissionManager.isBleEnabled()) {
            val enableBleIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (BleSDkPermissionManager.isGrantBleConnect()) {
                activity?.startActivityForResult(enableBleIntent, REQUEST_ENABLE_BLE)
            }
        }
    }

    /**
     * 低功耗蓝牙扫描
     */
    @SuppressLint("MissingPermission")
    fun startScan(tag: String, bleScanCallback: IBleScanCallback?) {
        if (!BleSDkPermissionManager.isGrantScanAllPermissions()) {
            mIsScanning = false
            return
        }
        mIsScanning = true
        if (mScanCallback == null) {
            mScanCallback = object: ScanCallback() {
                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    val mutableList = mutableListOf<BleScanResult>()
                    results?.forEach {
                        val bleScanResult = it.toBleScanResult()
                        mutableList.add(bleScanResult)
                    }
                    mBleScanCallbackMap.forEach { (_, scanCallback) ->
                        scanCallback?.onBatchScanResults(mutableList)
                    }
                }

                override fun onScanResult(callbackType: Int, bleScanResult: ScanResult?) {
                    val type =callbackType.toScanSettings()
                    mBleScanCallbackMap.forEach { (_, scanCallback) ->
                        scanCallback?.onScanResult(type.callbackType, bleScanResult?.toBleScanResult())
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    mBleScanCallbackMap.forEach { (_, scanCallback) ->
                        scanCallback?.onScanFailed(errorCode)
                    }
                }
            }
            mBleAdapter?.bluetoothLeScanner?.startScan(mScanCallback)
        }
        mBleScanCallbackMap[tag] = bleScanCallback
        mScanningMap[tag] = true
    }

    /**
     * 低功耗蓝牙扫描
     */
    @SuppressLint("MissingPermission")
    fun stopScan(tag: String?) {
        mIsScanning = false
        if (!BleSDkPermissionManager.isGrantScanAllPermissions()) {
            return
        }
        mBleScanCallbackMap.remove(tag)
        mScanningMap.remove(tag)
        if (mScanCallback != null && mBleScanCallbackMap.isEmpty()) {
            mBleAdapter?.bluetoothLeScanner?.stopScan(mScanCallback)
            mScanCallback = null
        }
    }

    fun stopScan(bleScanCallback: IBleScanCallback?) {
        var tag: String? = null
        mBleScanCallbackMap.forEach { (key, callback) ->
            if (bleScanCallback == callback) {
                tag = key
                return@forEach
            }
        }
        stopScan(tag)
    }
}