package com.soul.permission

import android.app.Dialog
import android.content.Context
import android.text.BoringLayout

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/07/19
 *     desc   :
 *     version: 1.0
 * </pre>
 */
abstract class BaseSingleCheckPermission: IBaseCheckPermission {
    private var mContext: Context? = null
    private var mIsHandleNext: Boolean = false
    private var mIsCustomPermissionDialog: Boolean? = null
    private var mPermissionCallback: ICommonCallback? = null
    private var mIsShowCancelDialog: Boolean? = null
    private var mCancelCallback: ICommonCallback? = null

    private var mPermission: Int = -1

    protected var mSkipSystemSetting: Int = -1

    protected var mCancelDialog: Dialog? = null

    protected var mPermissionDialog: Dialog? = null

    private var permissionDialogMap: Map<String, String>? = null

    constructor(context: Context?, permission: Int,
                permissionCallback: ICommonCallback, cancelCallback: ICommonCallback? = null,
                mCancelDialog: Dialog? = null, mPermissionDialog: Dialog? = null,
                isHandleNext: Boolean = false, isCustomPermissionDialog: Boolean = false,
                isShowCancelDialog: Boolean = false) {
        mContext = context
        mPermission = permission
        mIsHandleNext = isHandleNext
        mIsCustomPermissionDialog = isCustomPermissionDialog
        mPermissionCallback = permissionCallback
        mIsShowCancelDialog = isShowCancelDialog
        mCancelCallback = cancelCallback
        init()
    }

    fun init() {
        if (mCancelDialog == null) {

        }
    }

    fun setCancelDialog(cancelDialog: Dialog) {
        mCancelDialog = cancelDialog
    }

    fun setPermissionDialog(permissionDialog: Dialog) {
        mPermissionDialog = permissionDialog
    }

    class Custom
}