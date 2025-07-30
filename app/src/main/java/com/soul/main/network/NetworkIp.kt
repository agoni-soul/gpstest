package com.soul.main.network

import android.content.Context
import android.net.ConnectivityDiagnosticsManager
import android.net.Network
import android.net.NetworkInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.InetAddress

/**
 *
 * @author:     haha
 * @date:       2025/7/31
 * Description: 网络测试
 *
 **/
class NetworkIp {
    private val TAG = NetworkIp::class.simpleName

//    @RequiresApi(Build.VERSION_CODES.R)
//    private val mConnectivityDiagnosticsCallback = ExampleCallback()

//    private lateinit var mNetworkCallback: ConnectivityManager.NetworkCallback

//    private lateinit var mConnectivityDiagnosticsManager: ConnectivityDiagnosticsManager

    fun isWifiConnected(context: Context, networkInfo: NetworkInfo?) {
        if (networkInfo?.detailedState == NetworkInfo.DetailedState.OBTAINING_IPADDR
            || networkInfo?.detailedState == NetworkInfo.DetailedState.CONNECTING
        ) {
            Log.d(TAG, "WIFI_CONNECTING")
        } else if (networkInfo?.detailedState == NetworkInfo.DetailedState.CONNECTED) {
            Log.d(TAG, "WIFI_CONNECT")
        } else {
            Log.d(TAG, "WIFI_CONNECT_FAILED \t ${networkInfo?.detailedState}")
        }
    }

    fun pingforInetAddresss(ipAddress: String): Boolean {
        try {
            //超时应该在3秒以上
            val timeOut = 3000
            // 当返回值是true时，说明host是可用的，false则不可。
            val status = InetAddress.getByName(ipAddress).isReachable(timeOut)
            Log.d("haha", " try $status")
            return status
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        Log.d("haha", "end false")
        return false
    }

    /**
     * @hide
     */
    fun pingForCMD(ipAddress: String) {
        var line: String? = null
        try {
            val pro = Runtime.getRuntime().exec("ping $ipAddress")
            val buf = BufferedReader(
                InputStreamReader(
                    pro.inputStream
                )
            )
            line = buf.readLine()
            while (line != null) {
                Log.d("haha", line)
                line = buf.readLine()
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            Log.d("haha", ex.message + "")
        }
    }

    fun isNetworkOnline(): Boolean {
        val runtime = Runtime.getRuntime()
        var ipProcess: Process? = null
        try {
            ipProcess = runtime.exec("ping -c 5 -w 4 223.5.5.5")
            val input: InputStream = ipProcess.inputStream
            val `in` = BufferedReader(InputStreamReader(input))
            val stringBuffer = StringBuffer()
            var content: String? = ""
            while (`in`.readLine().also { content = it } != null) {
                stringBuffer.append(content)
            }
            val exitValue = ipProcess.waitFor()
            return if (exitValue == 0) {
                //WiFi连接，网络正常
                true
            } else {
                if (stringBuffer.indexOf("100% packet loss") != -1) {
                    Log.d("haha", "网络丢包严重，判断为网络未连接")
                    false
                } else {
                    Log.d("haha", "网络未丢包，判断为网络连接")
                    true
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            ipProcess?.destroy()
            runtime.gc()
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.R)
    class ExampleCallback : ConnectivityDiagnosticsManager.ConnectivityDiagnosticsCallback() {
        override fun onConnectivityReportAvailable(report: ConnectivityDiagnosticsManager.ConnectivityReport) {
            super.onConnectivityReportAvailable(report)
            Log.d("haha", "onConnectivityReportAvailable = ${report.network}")
        }

        override fun onDataStallSuspected(report: ConnectivityDiagnosticsManager.DataStallReport) {
            super.onDataStallSuspected(report)
            Log.d("haha", "onDataStallSuspected = ${report.network}")
        }

        override fun onNetworkConnectivityReported(network: Network, hasConnectivity: Boolean) {
            super.onNetworkConnectivityReported(network, hasConnectivity)
            Log.d("haha", "onNetworkConnectivityReported = $network \t $hasConnectivity")
        }
    }
}