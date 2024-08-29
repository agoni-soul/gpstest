package com.soul.blesdk.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.soul.blesdk.bean.BleScanResult
import com.soul.blesdk.bean.toBleScanResult
import com.soul.blesdk.constants.toScanSettings
import com.soul.blesdk.interfaces.IBleScanCallback
import com.soul.blesdk.permissions.BleSDkPermissionManager


/**
 *     author : haha
 *     time   : 2024-08-28
 *     desc   :
 *     version: 1.0
 */
class BleScanManager private constructor() : BaseBleManager() {
    val handler = Handler(Looper.getMainLooper())

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
    private var mIsClassicScanning = false
    private val mBleScanCallbackMap = mutableMapOf<String, IBleScanCallback?>()
    private val mScanCallbackMap = mutableMapOf<String, ScanCallback?>()
    private val mScanningMap = mutableMapOf<String, Boolean>()
    private var mScanCallback: ScanCallback? = null

    fun isScanning(): Boolean = mIsScanning

    fun isClassicScanning(): Boolean = mIsClassicScanning

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
            mIsClassicScanning = false
            return
        }
        mIsClassicScanning = true
        mBleAdapter?.startDiscovery()
    }

    /**
     * 经典蓝牙扫码
     */
//    @Deprecated("recommend to use stopScan()", ReplaceWith("stopScan(IBleScanCallback)"))
    @SuppressLint("MissingPermission")
    fun cancelDiscovery() {
        Log.d(TAG, "cancelDiscovery")
        mIsClassicScanning = false
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
     * 低功耗蓝牙扫描, 封装一层回调, 底层只会启动一次扫描回调
     *
     * @param
     */
    @SuppressLint("MissingPermission")
    fun startScan(tag: String, bleScanCallback: IBleScanCallback?) {
        if (!BleSDkPermissionManager.isGrantScanAllPermissions()) {
            mIsScanning = false
            return
        }
        mIsScanning = true
        if (mScanCallback == null) {
            mScanCallback = object : ScanCallback() {
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
                    val type = callbackType.toScanSettings()
                    mBleScanCallbackMap.forEach { (_, scanCallback) ->
                        scanCallback?.onScanResult(
                            type.callbackType,
                            bleScanResult?.toBleScanResult()
                        )
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
     * 自定义蓝牙扫描[scanFilters]和[scanSettings]参数,
     * 每次都单独创建一个新的监听扫描设备
     *
     * @param tag [bleScanCallback]回调标签, 用于停止扫描时移除回调
     * @param scanDurationTime 扫描时间, 扫描持续时间, 超时停止扫描, 非正数时主动调用才会停止
     * @param scanFilters 过滤扫描蓝牙设备的规则集合
     * @param scanSettings 定义蓝牙扫描参数规则
     * @param bleScanCallback 蓝牙扫描到设备回调
     *
     */
    @SuppressLint("MissingPermission")
    fun startScan(
        tag: String,
        scanDurationTime: Long,
        scanFilters: MutableList<ScanFilter>? = null,
        scanSettings: ScanSettings = ScanSettings.Builder().build(),
        bleScanCallback: IBleScanCallback?
    ) {
        if (!BleSDkPermissionManager.isGrantScanAllPermissions()) {
            mIsScanning = false
            return
        }
        val scanCallback = object : ScanCallback() {
            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                val mutableList = mutableListOf<BleScanResult>()
                results?.forEach {
                    val bleScanResult = it.toBleScanResult()
                    mutableList.add(bleScanResult)
                }
                bleScanCallback?.onBatchScanResults(mutableList)
            }

            override fun onScanResult(callbackType: Int, bleScanResult: ScanResult?) {
                val type = callbackType.toScanSettings()
                bleScanCallback?.onScanResult(
                    type.callbackType,
                    bleScanResult?.toBleScanResult()
                )
            }

            override fun onScanFailed(errorCode: Int) {
                bleScanCallback?.onScanFailed(errorCode)
            }
        }
        if (scanDurationTime > 0) {
            handler.postDelayed({
                stopScan(tag)
            }, scanDurationTime)
        }
        mBleAdapter?.bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
        mBleScanCallbackMap[tag] = bleScanCallback
        mScanningMap[tag] = true
        mScanCallbackMap[tag] = scanCallback
    }

    /**
     * 低功耗蓝牙扫描
     */
    @SuppressLint("MissingPermission")
    fun stopScan(tag: String?) {
        if (!BleSDkPermissionManager.isGrantScanAllPermissions()) {
            return
        }
        mBleScanCallbackMap.remove(tag)
        mScanningMap.remove(tag)
        val scanCallback = mScanCallbackMap.remove(tag)
        if (scanCallback != null) {
            mBleAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        }
        if (mScanCallback != null && mBleScanCallbackMap.isEmpty()) {
            mIsScanning = false
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