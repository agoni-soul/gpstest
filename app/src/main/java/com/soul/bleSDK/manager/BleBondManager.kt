package com.soul.bleSDK.manager

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import com.soul.bean.BleScanResult
import com.soul.bleSDK.permissions.BleSDkPermissionManager


/**
 *     author : yangzy33
 *     time   : 2024-08-02
 *     desc   :
 *     version: 1.0
 */
object BleBondManager: BaseBleManager() {

    @SuppressLint("MissingPermission")
    fun createBond(bleScanResult: BleScanResult?) {
        if (!BleSDkPermissionManager.isGrantConnectRelatedPermissions()) {
            return
        }
        mBleAdapter?.getRemoteDevice(bleScanResult?.device?.address)
            ?.createBond()
    }

    fun removeBond(bleScanResult: BleScanResult?) {
        bleScanResult ?: return
        bleScanResult.device ?: return
        try {
            val m = BluetoothDevice::class.java.getMethod("removeBond")
            m.invoke(bleScanResult.device)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "onClickUnbind: errorMessage = ${e.message}")
            e.printStackTrace()
        }
    }
}