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
interface ICommonCallback {
    /**
     * 取消开启权限
     *
     * @param permission 权限的类型，取值参考[CheckPermissionConstant]
     */
    fun leftEvent(permission: Int)

    /**
     * 开启权限
     *
     * @param permission 权限的类型，取值参考[CheckPermissionConstant]
     */
    fun rightEvent(permission: Int)
}