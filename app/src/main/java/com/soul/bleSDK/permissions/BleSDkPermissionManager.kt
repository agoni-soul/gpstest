package com.soul.bleSDK.permissions

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import com.soul.bleSDK.manager.BaseBleManager
import com.soul.bleSDK.manager.BleScanManager
import com.soul.log.DOFLogUtil
import com.soul.util.PermissionUtils

/**
 *
 * @author haha
 * @date 2024-08-26
 * @version 1.0
 *
 */
object BleSDkPermissionManager: BaseBleManager() {

    /**
     * 打开手机蓝牙
     *
     * @return true 表示打开成功
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun isEnableBle(): Boolean {
        if (mBleAdapter == null) { // 不支持蓝牙
            return false
        } else if (!mBleAdapter!!.isEnabled) {
            //若未打开手机蓝牙，则会弹出一个系统的是否打开/关闭蓝牙的对话框，禁止或者未处理返回false，允许返回true
            //若已打开手机蓝牙，直接返回true
            val enableState: Boolean = mBleAdapter!!.enable()
            Log.d(TAG, "（用户操作）手机蓝牙是否打开成功：$enableState")
            return enableState
        } else {
            return true
        }
    }

    fun isSupportBleConnect(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            false
        }
    }

    fun isOpenLocation(): Boolean {
        return PermissionUtils.checkMultiPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}