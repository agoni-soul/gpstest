package com.soul.permission

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/07/19
 *     desc   :
 *     version: 1.0
 * </pre>
 */
interface IBaseCheckPermission {

    /**
     * 在Activity的onStart()中检测权限
     */
    fun onStart()

    /**
     * 在Activity的onStart()中停止检测权限
     */
    fun onStop()

    /**
     * 检查当前权限是否开启，并执行权限开启或后续权限
     */
//    fun handlePermission(permissionFilter: PermissionFilter, permissionProxy: PermissionFilterProxy?)
}