package com.soul.animation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import com.airbnb.lottie.BuildConfig
import java.util.*


/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/10/27
 *     desc   :
 *     version: 1.0
 * </pre>
 */
object PermissionUtil {
    fun gotoPermission(context: Context) {
        val brand = Build.BRAND
        val brandLower = brand.toLowerCase(Locale.ROOT)
        if (TextUtils.equals(brandLower, "redmi") || TextUtils.equals(brandLower, "xiaomi")) { // 可行
            gotoMiuiPermission(context)
        } else if (TextUtils.equals(brandLower, "meizu")) {
            gotoMeizuPermission(context)
//        } else if (TextUtils.equals(brandLower, "huawei")) {
//            gotoHuaweiPermission(context)
//        } else if (TextUtils.equals(brandLower, "honor")){
//            gotoHuaweiPermission(context)
        } else if (TextUtils.equals(brandLower, "oppo")) { // 可行
            gotoOppoPermission(context)
        } else if (TextUtils.equals(brandLower, "vivo")) { // 可行
            gotoVivoPermission(context)
        } else {
            // vivo可以
            context.startActivity(getAppDetailSettingIntent(context))
        }
    }

    fun gotoMiuiPermission(context: Context) {
        try {
            val localIntent = Intent("miui.intent.action.APP_PERM_EDITOR")
            localIntent.setClassName(
                "com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity"
            )
            localIntent.putExtra("extra_pkgname", context.packageName)
            context.startActivity(localIntent)
        } catch (e: Exception) {
            try {
                val localIntent = Intent("miui.intent.action.APP_PERM_EDITOR")
                localIntent.setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.AppPermissionsEditorActivity"
                )
                localIntent.putExtra("extra_pkgname", context.packageName)
                context.startActivity(localIntent)
            } catch (e1: Exception) {
                context.startActivity(getAppDetailSettingIntent(context))
            }
        }
    }

    fun gotoMeizuPermission(context: Context) {
        try {
            val intent = Intent("com.meizu.safe.security.SHOW_APPSEC")
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.putExtra("packageName", context.packageName)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            context.startActivity(getAppDetailSettingIntent(context))
        }
    }

    fun gotoHuaweiPermission(context: Context) {
        try {
            val intent = Intent()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("packageName", BuildConfig.APPLICATION_ID)
            val comp = ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity")
            intent.component = comp
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            context.startActivity(getAppDetailSettingIntent(context))
        }
    }

    fun gotoOppoPermission(context: Context) {
        try {
            val intent = Intent()
            intent.putExtra("packageName", context.packageName)
            intent.component = ComponentName("com.color.safecenter",
                "com.color.safecenter.permission.PermissionManagerActivity")
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            context.startActivity(getAppDetailSettingIntent(context))
        }
    }

    private fun gotoVivoPermission(context: Context) {
        val localIntent: Intent
        if ((Build.MODEL.contains("Y85") && !Build.MODEL.contains("Y85A"))
            || Build.MODEL.contains("vivo Y53L")) {
            localIntent = Intent()
            localIntent.setClassName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.PurviewTabActivity"
            )
            localIntent.putExtra("packagename", context.packageName)
            localIntent.putExtra("tabId", "1")
            context.startActivity(localIntent)
        } else {
            localIntent = Intent()
            localIntent.setClassName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity"
            )
            localIntent.action = "secure.intent.action.softPermissionDetail"
            localIntent.putExtra("packagename", context.packageName)
            context.startActivity(localIntent)
        }
    }

    /**
     * vivo x20A,
     * huawei nova9 harmonyOS,
     * honor bkl-Al20
     */
    private fun getAppDetailSettingIntent(context: Context): Intent {
        val localIntent = Intent()
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        localIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
        localIntent.data = Uri.fromParts("package", context.packageName, null)

        return localIntent
    }

    private fun goHuaWeiMainager(context: Context) {
        try {
            val intent = Intent(context.packageName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val comp = ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity")
            intent.component = comp
            context.startActivity(intent)
        } catch (e : Exception) {
            e.printStackTrace()
            getAppDetailSettingIntent(context)
        }
    }
}