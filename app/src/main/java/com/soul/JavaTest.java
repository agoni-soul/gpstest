package com.soul;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/12/19
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class JavaTest {
    void test(Context context) {
        WifiManager wifiManager = (WifiManager) (context.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        List<ScanResult> list = wifiManager.getScanResults();
        for (int i = 0; i < list.size(); i ++) {
            ScanResult scanResult = list.get(i);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Log.d("haha", "ssid = " + scanResult.SSID +
                        ", capabilities = " + scanResult.capabilities + ", wifiStandard = " + scanResult.getWifiStandard());
            }
        }
    }
}
