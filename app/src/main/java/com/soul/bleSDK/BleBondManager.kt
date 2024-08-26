package com.soul.bleSDK

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.util.Log
import com.soul.bean.BleScanResult
import com.soul.bleSDK.manager.BleScanManager
import com.soul.util.PermissionUtils


/**
 *     author : yangzy33
 *     time   : 2024-08-02
 *     desc   :
 *     version: 1.0
 */
object BleBondManager {
    private val TAG = this.javaClass::class.java.simpleName

    fun createBond(bleScanResult: BleScanResult?) {
        if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        BleScanManager.getBluetoothAdapter()
            ?.getRemoteDevice(bleScanResult?.device?.address)
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