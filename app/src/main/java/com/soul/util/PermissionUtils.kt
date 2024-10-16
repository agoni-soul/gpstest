package com.soul.util

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.Utils
import com.soul.SoulApplication

/**
 * <pre>
 *     author : haha
 *     time   : 2023/04/03
 *     desc   :
 *     version: 1.0
 * </pre>
 */
object PermissionUtils {

    private val TAG = javaClass.simpleName

    var permissionDenyList = arrayListOf<String>()

    /**
     * 检查权限是否开启
     *
     * @param permission 权限名
     */
    fun checkSinglePermission(permission: String): Boolean {
        synchronized(permissionDenyList) {
            permissionDenyList.clear()
        }
        val context = SoulApplication.application ?: Utils.getApp()
        val isGrant = ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "checkSinglePermission: ${permission}: isGrant = $isGrant")
        if (!isGrant) {
            synchronized(permissionDenyList) {
                permissionDenyList.add(permission)
            }
        }
        return isGrant
    }

    /**
     * 检查某一权限是否开启
     *
     * @param permissions 权限名
     */
    fun checkMultiPermission(vararg permissions: String): Boolean {
        synchronized(permissionDenyList) {
            permissionDenyList.clear()
        }
        var areGrantAllPermissions = true
        synchronized(permissionDenyList) {
            for (permission in permissions) {
                val context = SoulApplication.application ?: Utils.getApp()
                val isGrant = ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                if (!isGrant) {
                    permissionDenyList.add(permission)
                }
                areGrantAllPermissions = areGrantAllPermissions.and(isGrant)
            }
        }
        return areGrantAllPermissions
    }

    fun requestPermissions(activity: Activity?, vararg permissions: String) {
        if (activity == null) return
        val intent = Intent(activity, PermissionTransparentActivity::class.java)
        intent.putExtra(PermissionTransparentActivity.DENY_PERMISSION, permissions)
        activity.startActivity(intent)
    }

    fun checkGPSPermission(): Boolean = checkMultiPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
}