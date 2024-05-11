package com.soul.gps

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.soul.base.BaseActivity
import com.soul.gpstest.R

class GpsActivity : BaseActivity() {

    companion object {
        const val ACCESS_FIND_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION

        const val ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    }

    private val mContext: Context by lazy {
        this
    }

    private val mTvLocation: TextView by lazy {
        findViewById(R.id.text_gps)
    }
    private val mBtnLocationShow: Button by lazy {
        findViewById(R.id.btn_gps_show)
    }
    private val mBtnLocationReset: Button by lazy {
        findViewById(R.id.btn_gps_reset)
    }
    private val mBtnCoarseLocation: Button by lazy {
        findViewById(R.id.btn_coarse_location)
    }
    private val mBtnFindLocation: Button by lazy {
        findViewById(R.id.btn_find_location)
    }
    private val mTvConnectWifiInfo: TextView by lazy {
        findViewById(R.id.tv_connect_wifi)
    }
    private val mBtnRefreshWifi: TextView by lazy {
        findViewById(R.id.btn_refresh_wifi)
    }

    private val mLocationManager: LocationManager by lazy {
        this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private var mLocationProvider: String? = null

    private val mWifiManager: WifiManager by lazy {
        applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    }

    private val mListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            showLocation(location)
        }

        override fun onProviderDisabled(provider: String) {
            showLocation(null)
        }

        override fun onProviderEnabled(provider: String) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext, "定位权限未开启", Toast.LENGTH_SHORT).show()
                return
            }
            showLocation(mLocationManager.getLastKnownLocation(provider))
        }
    }

    override fun onStart() {
        super.onStart()
        if (isGPSAble()) {
            initData()
        } else {
            openGPS()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_gps
    }

    override fun initView() {
        mBtnLocationReset.setOnClickListener {
            mTvLocation.text = "默认定位信息"
        }
        mBtnLocationShow.setOnClickListener {
            initData()
        }
        mBtnCoarseLocation.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val dialog = AlertDialog.Builder(this)
                dialog.apply {
                    setTitle("请求模糊定位权限")
                    setMessage("获取位置经纬度需要获取模糊权限")
                    setPositiveButton("同意") { _, _ ->
                        ActivityCompat.requestPermissions(
                            this@GpsActivity,
                            arrayOf(ACCESS_COARSE_LOCATION),
                            200
                        )
                    }
                    setNegativeButton("拒绝", null)
                }
                dialog.show()
            } else {
                Toast.makeText(this, "模糊定位权限已经开启", Toast.LENGTH_SHORT).show()
            }
        }
        mBtnFindLocation.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FIND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val dialog = AlertDialog.Builder(this)
                dialog.apply {
                    setTitle("请求GPS精准定位")
                    setMessage("获取位置经纬度需要获取GPS精准权限")
                    setPositiveButton("同意") { _, _ ->
                        ActivityCompat.requestPermissions(
                            this@GpsActivity,
                            arrayOf(ACCESS_FIND_LOCATION),
                            200
                        )
                    }
                    setNegativeButton("拒绝", null)
                }
                dialog.show()
            } else {
                Toast.makeText(this, "精准定位权限已经开启", Toast.LENGTH_SHORT).show()
            }
        }
        mBtnRefreshWifi.setOnClickListener {
            val scanResult = mWifiManager.scanResults
            val connectInfo = mWifiManager.connectionInfo
            mTvConnectWifiInfo.text =
                if (scanResult.size > 0) "${connectInfo.ssid}\t 搜索到wifi数：${scanResult.size}"
                else "无法获取wifi信息"
        }
    }

    override fun initData() {
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "定位权限未开启", Toast.LENGTH_SHORT).show()
            return
        }

        val provides = mLocationManager.getProviders(true)
        mLocationProvider = if (provides.contains(LocationManager.NETWORK_PROVIDER)) {
            LocationManager.NETWORK_PROVIDER
        } else if (provides.contains(LocationManager.GPS_PROVIDER)) {
            LocationManager.GPS_PROVIDER
        } else {
            Toast.makeText(this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show()
            return
        }
        mLocation = mLocationManager.getLastKnownLocation(mLocationProvider!!)
        if (mLocation != null) {
            showLocation(mLocation!!)
        } else {
            mLocationManager.requestLocationUpdates(mLocationProvider!!, 0, 0f, mListener)
        }
    }

    private var mLocation: Location? = null

    private fun showLocation(location: Location?) {
        if (location == null) {
            mTvLocation.text = "无法获取"
        } else {
            val address = "latitude = ${location.latitude}, longitude = ${location.longitude}"
            mTvLocation.text = address
        }
    }

    /**
     * GPS定位权限是否开启
     */
    private fun isGPSAble(): Boolean =
        mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    private fun openGPS() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivityForResult(intent, 0)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            200 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initData()
                } else {
                    val intent = Intent()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                    intent.data = Uri.fromParts("package", packageName, null)
                    startActivity(intent)
                }
            }
            else -> {

            }
        }
    }
}