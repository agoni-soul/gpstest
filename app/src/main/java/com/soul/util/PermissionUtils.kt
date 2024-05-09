package com.soul.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.Utils

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2023/04/03
 *     desc   :
 *     version: 1.0
 * </pre>
 */
object PermissionUtils {

    var permissionDenyList = arrayListOf<String>()

    fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(Utils.getApp(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }

    /**
     * 检查权限是否开启
     *
     * @param permission 权限名
     */
    fun checkPermission(permission: String): Boolean {
        permissionDenyList.clear()
        val isGrant = ActivityCompat.checkSelfPermission(Utils.getApp(), permission) == PackageManager.PERMISSION_GRANTED
        if (!isGrant) {
            permissionDenyList.add(permission)
        }
        return isGrant
    }

    /**
     * 检查某一权限是否开启
     *
     * @param permissions 权限名
     */
    fun checkPermission(vararg permissions: String): Boolean {
        permissionDenyList.clear()
        var areGrantAllPermissions = true
        for (permission in permissions) {
            val isGrant = ActivityCompat.checkSelfPermission(Utils.getApp(), permission) == PackageManager.PERMISSION_GRANTED
            if (!isGrant) {
                permissionDenyList.add(permission)
            }
            areGrantAllPermissions = areGrantAllPermissions.and(isGrant)
        }
        return areGrantAllPermissions
    }

    fun requestPermissions(activity: Activity?, vararg permissions: String) {
        if (activity == null) return
        val intent = Intent(activity, PermissionTransparentActivity::class.java)
        intent.putExtra(PermissionTransparentActivity.DENY_PERMISSION, permissions)
        activity.startActivity(intent)
    }

    fun checkGPSPermission(): Boolean = checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
}