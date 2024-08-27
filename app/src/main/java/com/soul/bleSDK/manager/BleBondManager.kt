package com.soul.bleSDK.manager

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.soul.bean.BleScanResult
import com.soul.bleSDK.manager.BleScanManager
import com.soul.util.PermissionUtils


/**
 *     author : yangzy33
 *     time   : 2024-08-02
 *     desc   :
 *     version: 1.0
 */
object BleBondManager: BaseBleManager() {

    fun createBond(bleScanResult: BleScanResult?) {
        if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_CONNECT)) {
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