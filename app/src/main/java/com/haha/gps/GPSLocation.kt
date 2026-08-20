package com.haha.gps

import android.location.Location
import android.location.LocationListener
import android.location.LocationProvider
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * 类描述：实现LocationListener的子类，同时实现自己的接口调用
 * Created by lizhenya on 2016/9/12.
 */
class GPSLocation(gpsLocationListener: GPSLocationListener) : LocationListener {

    private val listenerRef = WeakReference(gpsLocationListener)

    fun clearListener() {
        listenerRef.clear()
    }

    override fun onLocationChanged(location: Location) {
        listenerRef.get()?.updateLocation(location)
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        val listener = listenerRef.get() ?: return
        listener.updateStatus(provider, status, extras)
        when (status) {
            LocationProvider.AVAILABLE -> listener.updateGPSProviderStatus(
                GPSProviderStatus.GPS_AVAILABLE
            )

            LocationProvider.OUT_OF_SERVICE -> listener.updateGPSProviderStatus(
                GPSProviderStatus.GPS_OUT_OF_SERVICE
            )

            LocationProvider.TEMPORARILY_UNAVAILABLE -> listener.updateGPSProviderStatus(
                GPSProviderStatus.GPS_TEMPORARILY_UNAVAILABLE
            )
        }
    }

    override fun onProviderEnabled(provider: String) {
        listenerRef.get()?.updateGPSProviderStatus(GPSProviderStatus.GPS_ENABLED)
    }

    override fun onProviderDisabled(provider: String) {
        listenerRef.get()?.updateGPSProviderStatus(GPSProviderStatus.GPS_DISABLED)
    }
}
