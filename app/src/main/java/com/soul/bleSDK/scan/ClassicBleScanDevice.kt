package com.soul.bleSDK.scan

import android.Manifest
import android.util.Log
import com.soul.log.DOFLogUtil
import com.soul.util.PermissionUtils


/**
 *     author : yangzy33
 *     time   : 2024-08-20
 *     desc   :
 *     version: 1.0
 */
class ClassicBleScanDevice: BaseBleScanDevice() {

    override fun startScan() {
        Log.d(TAG, "startDiscovery")
        if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_SCAN)) {
            DOFLogUtil.d(TAG, "Manifest.permission.BLUETOOTH_SCAN: PERMISSION_DENIED")
            return
        }
        mBleAdapter?.startDiscovery()
    }

    override fun stopScan() {
        Log.d(TAG, "stopScan")
        if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_SCAN)) {
            DOFLogUtil.d(TAG, "Manifest.permission.BLUETOOTH_SCAN: PERMISSION_DENIED")
            return
        }
        mBleAdapter?.cancelDiscovery()
    }
}