package com.soul.gps

import android.location.Location
import android.location.LocationListener
import android.location.LocationProvider
import android.os.Bundle
import com.soul.gpstest.GPSProviderStatus


/**
 * 类描述：实现LocationListener的子类，同时实现自己的接口调用
 * Created by lizhenya on 2016/9/12.
 */
class GPSLocation(private val mGpsLocationListener: GPSLocationListener) : LocationListener {
    override fun onLocationChanged(location: Location) {
        if (location != null) {
            mGpsLocationListener.updateLocation(location)
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        mGpsLocationListener.updateStatus(provider, status, extras)
        when (status) {
            LocationProvider.AVAILABLE -> mGpsLocationListener.updateGPSProviderStatus(GPSProviderStatus.GPS_AVAILABLE)
            LocationProvider.OUT_OF_SERVICE -> mGpsLocationListener.updateGPSProviderStatus(GPSProviderStatus.GPS_OUT_OF_SERVICE)
            LocationProvider.TEMPORARILY_UNAVAILABLE -> mGpsLocationListener.updateGPSProviderStatus(GPSProviderStatus.GPS_TEMPORARILY_UNAVAILABLE)
        }
    }

    override fun onProviderEnabled(provider: String) {
        mGpsLocationListener.updateGPSProviderStatus(GPSProviderStatus.GPS_ENABLED)
    }

    override fun onProviderDisabled(provider: String) {
        mGpsLocationListener.updateGPSProviderStatus(GPSProviderStatus.GPS_DISABLED)
    }
}
