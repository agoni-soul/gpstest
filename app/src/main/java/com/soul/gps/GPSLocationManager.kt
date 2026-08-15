package com.soul.gps

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.lang.ref.WeakReference

/**
 * 类描述：GPS定位的管理类
 * Created by lizhenya on 2016/9/12.
 */
class GPSLocationManager private constructor(context: Activity) {
    private var isGpsEnabled = false
    private var mContext: WeakReference<Activity?>? = null
    private var locationManager: LocationManager? = null
    private var mGPSLocation: GPSLocation? = null
    private var isOPenGps = false
    private var mMinTime: Long = 0
    private var mMinDistance = 0f

    private fun refreshContext(context: Activity) {
        initData(context)
    }

    private fun initData(context: Activity) {
        mContext = WeakReference(context)
        // LocationManager 取自 application，避免单例长期依赖 Activity 的 SystemService 代理
        locationManager =
            context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // 默认不强制打开GPS设置面板
        isOPenGps = false
        // 默认定位时间间隔为1000ms
        mMinTime = 1000
        // 默认位置可更新的最短距离为0m
        mMinDistance = 0f
    }

    /**
     * 方法描述：设置发起定位请求的间隔时长
     *
     * @param minTime 定位间隔时长（单位ms）
     */
    fun setScanSpan(minTime: Long) {
        mMinTime = minTime
    }

    /**
     * 方法描述：设置位置更新的最短距离
     *
     * @param minDistance 最短距离（单位m）
     */
    fun setMinDistance(minDistance: Float) {
        mMinDistance = minDistance
    }

    /**
     * GPS 定位是否已开启
     */
    fun isGpsAble(): Boolean {
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    }

    /**
     * 优先 NETWORK，其次 GPS，与 GpsActivity 原逻辑一致
     */
    private fun resolveBestProvider(): String? {
        val manager = locationManager ?: return null
        val providers = manager.getProviders(true)
        return when {
            providers.contains(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            providers.contains(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            else -> null
        }
    }

    /**
     * 方法描述：开启定位（默认情况下不会强制要求用户打开GPS设置面板）
     *
     * @param gpsLocationListener
     * @param isOpenGps 当用户GPS未开启时是否强制用户开启GPS
     */
    @JvmOverloads
    fun start(gpsLocationListener: GPSLocationListener?, isOpenGps: Boolean = isOPenGps) {
        isOPenGps = isOpenGps
        val activity = mContext?.get() ?: return
        val manager = locationManager ?: return
        if (gpsLocationListener == null) {
            return
        }

        // 先停掉旧监听，避免重复注册导致泄漏
        stop()

        isGpsEnabled = manager.isProviderEnabled(GPS_LOCATION_NAME)
        if (!isGpsEnabled && isOPenGps) {
            openGPS()
            return
        }

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val locateType = resolveBestProvider()
        if (locateType == null) {
            Toast.makeText(activity, "没有可用的位置提供器", Toast.LENGTH_SHORT).show()
            return
        }
        mLocateType = locateType

        val gpsLocation = GPSLocation(gpsLocationListener)
        mGPSLocation = gpsLocation
        val lastKnownLocation = manager.getLastKnownLocation(locateType)
        if (lastKnownLocation != null) {
            gpsLocation.onLocationChanged(lastKnownLocation)
        }
        // 参数2和3：若参数3不为0，以参数3为准；参数3为0则按时间更新；两者为0则随时刷新
        manager.requestLocationUpdates(locateType, mMinTime, mMinDistance, gpsLocation)
    }

    /**
     * 方法描述：转到手机设置界面，用户设置GPS
     */
    fun openGPS() {
        val activity = mContext?.get() ?: return
        Toast.makeText(activity, "请打开GPS设置", Toast.LENGTH_SHORT).show()
        if (Build.VERSION.SDK_INT > 15) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activity.startActivityForResult(intent, 0)
        }
    }

    /**
     * 方法描述：终止GPS定位，建议在 onPause()/onDestroy() 中调用
     */
    fun stop() {
        val gpsLocation = mGPSLocation
        val manager = locationManager
        if (gpsLocation != null && manager != null) {
            try {
                manager.removeUpdates(gpsLocation)
            } catch (_: Exception) {
                // 无权限或未注册时忽略，确保下方引用仍被清空
            }
        }
        // 无论权限/Activity 是否可用，都必须断开监听引用，避免单例泄漏
        mGPSLocation?.clearListener()
        mGPSLocation = null
    }

    companion object {
        private const val GPS_LOCATION_NAME = LocationManager.GPS_PROVIDER
        private var gpsLocationManager: GPSLocationManager? = null
        private val objLock = Any()
        private var mLocateType: String? = null

        fun getInstances(context: Activity): GPSLocationManager {
            if (gpsLocationManager == null) {
                synchronized(objLock) {
                    if (gpsLocationManager == null) {
                        gpsLocationManager = GPSLocationManager(context)
                    }
                }
            } else {
                // 刷新 WeakReference，避免旧 Activity 已销毁后仍用过期引用
                gpsLocationManager?.refreshContext(context)
            }
            return gpsLocationManager!!
        }
    }

    init {
        initData(context)
    }
}
