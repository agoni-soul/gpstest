package com.soul.log.utils

import android.os.Build
import android.text.TextUtils

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2023/03/28
 *     desc   :
 *     version: 1.0
 * </pre>
 */
object SystemInfoUtil {
    private fun getABIs(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.SUPPORTED_ABIS.isNotEmpty()) {
            Build.SUPPORTED_ABIS
        } else {
            if (!TextUtils.isEmpty(Build.CPU_ABI2)) {
                arrayOf(Build.CPU_ABI, Build.CPU_ABI2)
            } else arrayOf(Build.CPU_ABI)
        }
    }

    //是否只支持armeabi
    fun isOnlySupportArmeabi(): Boolean {
        var result = true
        val abis = getABIs()
        for (abi in abis) {
            if (abi == "armeabi-v7a") {
                result = false
                break
            } else if (abi == "arm64-v8a") {
                result = false
                break
            }
        }
        return result
    }
}