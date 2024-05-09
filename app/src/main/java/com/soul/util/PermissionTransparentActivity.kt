package com.soul.util

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.soul.gpstest.R
import com.soul.log.DOFLogUtil
import com.soul.ui.dialog.CustomDialog

class PermissionTransparentActivity: Activity() {
    private val TAG = this.javaClass.name

    companion object {
        const val DENY_PERMISSION = "deny_permission"

        private const val REQUEST_CODE_PERMISSION = 0
    }

    private var dialog: CustomDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.permission_transparent_activity)
        var permissions: Array<String>? = null
        if (intent.hasExtra(DENY_PERMISSION)) {
            permissions = intent.getStringArrayExtra(DENY_PERMISSION)
        }
        if ((permissions == null) || permissions.isEmpty()) return
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION)
        dialog = CustomDialog(this)
        dialog?.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            val size = permissions.size
            for(index in 0 until size) {
                DOFLogUtil.d(TAG, "permission = {${permissions[index]}}, isGrant = ${grantResults[index]}")
            }
        }
    }
}