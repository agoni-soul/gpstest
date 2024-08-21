package com.soul.bleSDK.scan

import android.Manifest
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import com.soul.bean.BleScanResult
import com.soul.bean.toBleScanResult
import com.soul.bleSDK.constants.toScanSettings
import com.soul.log.DOFLogUtil
import com.soul.util.PermissionUtils


/**
 *     author : yangzy33
 *     time   : 2024-08-20
 *     desc   :
 *     version: 1.0
 */
class LowPowerBleScanDevice: BaseBleScanDevice() {
    private var mScanCallback: ScanCallback? = null

    override fun startScan() {
        if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_SCAN)) {
            DOFLogUtil.d(TAG, "Manifest.permission.BLUETOOTH_SCAN: PERMISSION_DENIED")
            mIsScanning = false
            return
        }
        if (mScanCallback == null) {
            mScanCallback =  object: ScanCallback() {
                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    val mutableList = mutableListOf<BleScanResult>()
                    results?.forEach {
                        val bleScanResult = it.toBleScanResult()
                        mutableList.add(bleScanResult)
                    }
                    mBleScanCallbacks.forEach {
                        it.onBatchScanResults(mutableList)
                    }
                }

                override fun onScanResult(callbackType: Int, bleScanResult: ScanResult?) {
                    val type = callbackType.toScanSettings().callbackType
                    mBleScanCallbacks.forEach {
                        it.onScanResult(type, bleScanResult?.toBleScanResult())
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    mBleScanCallbacks.forEach {
                        it.onScanFailed(errorCode)
                    }
                }
            }
        }
        mBleAdapter?.bluetoothLeScanner?.startScan(mScanCallback)
    }

    override fun stopScan() {
        mIsScanning = false
        if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_SCAN)) {
            DOFLogUtil.d(TAG, "Manifest.permission.BLUETOOTH_SCAN: PERMISSION_DENIED")
            return
        }
        if (mBleScanCallbacks.isNotEmpty()) {
            mBleScanCallbacks.removeLast()
        }
        if (mScanCallback != null && mBleScanCallbacks.isEmpty()) {
            mBleAdapter?.bluetoothLeScanner?.stopScan(mScanCallback)
            mScanCallback = null
        }
    }
}