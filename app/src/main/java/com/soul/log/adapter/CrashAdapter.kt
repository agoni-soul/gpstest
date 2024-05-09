package com.soul.log.adapter

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.CrashUtils
import com.blankj.utilcode.util.Utils

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2023/03/28
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class CrashAdapter : DefaultLogAdapter() {
    init {
        if (ActivityCompat.checkSelfPermission(
                Utils.getApp(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            CrashUtils.init()
        }
    }
}
