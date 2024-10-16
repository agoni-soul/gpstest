package com.soul.blesdk.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.util.Log
import com.blankj.utilcode.util.PermissionUtils
import com.soul.BleSDKApplication
import com.soul.blesdk.manager.BaseBleManager


/**
 *
 * @author haha
 * @date 2024-08-26
 * @version 1.0
 *
 */
object BleSDkPermissionManager: BaseBleManager() {

    /**
     * 蓝牙开关是否打开, [getBluetoothAdapter]为空则不支持蓝牙
     *
     * @return true 表示打开成功
     */
    @SuppressLint("MissingPermission")
    fun isBleEnabled(): Boolean {
        return mBleAdapter?.isEnabled ?: false
    }

    @SuppressLint("MissingPermission")
    fun enableBle(): Boolean {
        if (mBleAdapter == null) { // 不支持蓝牙
            return false
        } else if (!mBleAdapter!!.isEnabled) {
            //若未打开手机蓝牙，则会弹出一个系统的是否打开/关闭蓝牙的对话框，禁止或者未处理返回false，允许返回true
            //若已打开手机蓝牙，直接返回true
            val enableState: Boolean = if (isGrantBleConnect()) mBleAdapter!!.enable() else false
            Log.d(TAG, "isBleEnabled： （用户操作）手机蓝牙是否打开成功：$enableState")
            return enableState
        } else {
            return true
        }
    }

    fun isGrantBleConnect(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionUtils.isGranted(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            PermissionUtils.isGranted(Manifest.permission.BLUETOOTH_ADMIN)
        }
    }

    fun isGrantBleScan(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionUtils.isGranted(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            true
        }
    }

    /**
     * 获取Gps开启或关闭状态
     */
    fun isGpsEnabled(): Boolean {
        val locationManager = mApplication?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isGps = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        val isNetwork = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false
        Log.d(TAG, "isGpsEnabled: isGps = $isGps, isNetwork = $isNetwork")
        return isGps.or(isNetwork)
    }

    fun isGrantGPSLocation(): Boolean {
        return PermissionUtils.isGranted(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    fun isGrantNecessaryBle(): Boolean {
        return PermissionUtils.isGranted(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }


    /**
     * 蓝牙扫描必须的所有权限
     */
    fun isGrantScanAllPermissions(): Boolean {
        return isGrantNecessaryBle() && isGrantBleScan() && isGrantBleConnect()
                && isBleEnabled() && isGrantGPSLocation() && isGpsEnabled()
    }

    /**
     * 蓝牙连接所需的相关权限
     */
    fun isGrantConnectRelatedPermissions(): Boolean {
        return isGrantNecessaryBle() && isGrantBleConnect()
                && isBleEnabled() && isGrantGPSLocation() && isGpsEnabled()
    }

    /**
     * 蓝牙扫码所需的相关权限
     */
    fun isGrantScanRelatedPermissions(): Boolean {
        return isGrantNecessaryBle() && isGrantBleScan()
                && isBleEnabled() && isGrantGPSLocation() && isGpsEnabled()
    }
}