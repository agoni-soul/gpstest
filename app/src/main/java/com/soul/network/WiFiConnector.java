package com.midea.iot.msmart.network;


import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.PatternMatcher;
import android.text.TextUtils;


import com.midea.iot.msmart.MSContext;
import com.midea.iot.msmart.common.utils.LogUtils;
import com.midea.iot.msmart.common.utils.ReflectUtil;

import java.lang.reflect.Method;
import java.util.List;

import androidx.annotation.RequiresApi;

/**
 * WiFi connector.
 * Created by seagle on 2018/4/23.
 *
 * @author yuanxiudong66@sina.com
 * @since 2018-4-23
 */
class WiFiConnector {
    private static final int SECURITY_NONE = 0;
    private static final int SECURITY_WEP = 1;
    private static final int SECURITY_PSK = 2;
    private static final int SECURITY_EAP = 3;

    private WifiManager mWifiManager;
    private ScanResult mScanResult;
    private String mSSID;
    private String mCapabilities;
    private String mPassword;

    /**
     * WifiManager类的影藏方法public void connect(int networkID, ActionListener listener)
     */
    private Method mConnectMethod2;

    /**
     * WifiManager类的影藏方法public void connect(WifiConfiguration configuration, ActionListener listener)
     */
    private Method mConnectMethod;

    private boolean isHidden;

    //android Q以上使用
    private ConnectivityManager.NetworkCallback mNetworkCallback;

    private ConnectivityManager.NetworkCallback mRequestCallback;

    WiFiConnector(ScanResult scanResult, String password, WifiManager wifiManager) {
        mScanResult = scanResult;
        mSSID = scanResult.SSID;
        mCapabilities = scanResult.capabilities;
        mPassword = password;
        mWifiManager = wifiManager;
        init();
    }

    WiFiConnector(String SSID, String capabilities, String password, WifiManager wifiManager) {
        mScanResult = null;
        mSSID = SSID;
        isHidden = false;
        mCapabilities = capabilities;
        mPassword = password;
        mWifiManager = wifiManager;
        init();
    }

    WiFiConnector(String SSID, String capabilities, String password, boolean isHidden, WifiManager wifiManager) {
        mScanResult = null;
        mSSID = SSID;
        this.isHidden = isHidden;
        mCapabilities = capabilities;
        mPassword = password;
        mWifiManager = wifiManager;
        init();
    }

    private void init() {
        for (Method methodSub : mWifiManager.getClass().getDeclaredMethods()) {
            if ("connect".equalsIgnoreCase(methodSub.getName())) {
                Class<?>[] types = methodSub.getParameterTypes();
                if (types != null && types.length > 0) {
                    if ("android.net.wifi.WifiConfiguration".equalsIgnoreCase(types[0].getName())) {
                        mConnectMethod = methodSub;
                        mConnectMethod.setAccessible(true);
                    }
                    if ("int".equalsIgnoreCase(types[0].getName())) {
                        mConnectMethod2 = methodSub;
                        mConnectMethod2.setAccessible(true);
                    }
                    if (mConnectMethod != null && mConnectMethod2 != null)
                        break;
                }
            }
        }
    }

    public String getSSID() {
        return mSSID;
    }

    public static String convertToQuotedString(String ssid) {
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            return ssid;
        }
        return "\"" + ssid + "\"";
    }

    private static int getSecurity(String capabilities) {
        if (TextUtils.isEmpty(capabilities)) return SECURITY_NONE;
        if (capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    public int connect() {
        WifiConfiguration wifiConfiguration = getExistedConfiguration(mSSID);
        LogUtils.i("WiFiConnector", "connect " + mSSID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (MSContext.getInstance().getContext().getApplicationInfo().targetSdkVersion >= 29) {
                return connectWifiQ();
            } else {
                return connectWifiNormal(wifiConfiguration);
            }
        } else if ((mConnectMethod2 == null && mConnectMethod == null) || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
                || Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {//9.0因为反射不建议使用的原因，有的品牌手机使用反射已经不能主动连接wifi了，应该是源码某个变量做了来处理了
            return connectWifiNormal(wifiConfiguration);
        } else {
            return connectWifiReflect(wifiConfiguration == null ? getConfig() : wifiConfiguration, wifiConfiguration);
        }
    }

    /***
     * 释放上一次连接的Wi-Fi资源，Android Q以上才能用
     * @return
     */
    public boolean releaseWifiConnector() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (mRequestCallback != null) {
                NetworkMonitor.getInstance().getConnectivityManager().unregisterNetworkCallback(mRequestCallback);
                mRequestCallback = null;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private int connectWifiQ() {
        WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder()
                .setIsHiddenSsid(isHidden)
                .setSsid(mSSID);//open版本会导致系统扫描不出来热点
//                .setSsidPattern(new PatternMatcher(mSSID, PatternMatcher.PATTERN_PREFIX));
//                        .setBssidPattern(MacAddress.fromString("10:03:23:00:00:00"), MacAddress.fromString("ff:ff:ff:00:00:00"))
//        if (TextUtils.isEmpty(mPassword)) {
//            builder.setIsEnhancedOpen(true);//open版本会出现这个出来热点的情况
//        } else
        int security = getSecurity(mCapabilities);
        if (security == SECURITY_NONE || TextUtils.isEmpty(mPassword)) {//兼容上传传参错误的处理，如果密码为空，默认不加密
            builder.setWpa2Passphrase("");
        } else {
            if (!TextUtils.isEmpty(mCapabilities) && mCapabilities.contains("WPA3"))
                builder.setWpa3Passphrase(mPassword);
            else
                builder.setWpa2Passphrase(mPassword);
        }


        final NetworkSpecifier specifier = builder.build();

        final NetworkRequest request =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .setNetworkSpecifier(specifier)
                        .build();
        if (mRequestCallback == null) {
            mRequestCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    LogUtils.d("WiFiConnector", "android Q连接成功");
                    super.onAvailable(network);
                    if (mNetworkCallback != null) {
                        mNetworkCallback.onAvailable(network);
                    }
                }

                @Override
                public void onUnavailable() {
                    LogUtils.d("WiFiConnector", "android Q连接失败，但是我不提示");
                    super.onUnavailable();
//                    if (mNetworkCallback != null) {
//                        mNetworkCallback.onUnavailable();
//                    }
                }
            };
        }
        NetworkMonitor.getInstance().getConnectivityManager().requestNetwork(request, mRequestCallback);
        return 1000;
    }


    /***
     * 正常网络连接
     * @param wifiConfiguration
     * @return
     */
    private int connectWifiNormal(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration == null) {
            LogUtils.i("WiFiConnector", "wifiConfiguration=null");
            wifiConfiguration = getConfig();
            LogUtils.i("WiFiConnector", "getConfig()=" + wifiConfiguration.toString());
            int netID = mWifiManager.addNetwork(wifiConfiguration);
            LogUtils.i("WiFiConnector", "netID=" + netID);
            if (netID > 0 && mWifiManager.enableNetwork(netID, true)) {
                LogUtils.i("WiFiConnector", "enableNetWork");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    LogUtils.i("WiFiConnector", "Build.VERSION.SDK_INT >= Build.VERSION_CODES.N");
                    try {
                        mWifiManager.saveConfiguration();
                        LogUtils.i("WiFiConnector", "saveConfiguration()");
                    } catch (Exception e) {//deadsystemexception这里无法catch,这个方式不知道能不能解决，因为这里华为系统8.0以上会出现系统进程崩溃，具体原因无法了解
                        LogUtils.e("WiFiConnector", e.getMessage());
                    }
                } else {
                    LogUtils.i("WiFiConnector", "Build.VERSION.SDK_INT < Build.VERSION_CODES.N");
                    mWifiManager.saveConfiguration();
                    LogUtils.i("WiFiConnector", "saveConfiguration()");
                }
                mWifiManager.reconnect();
                LogUtils.i("WiFiConnector", "reconnect");
                return netID;
            } else {
                return -1;
            }
        } else {
            LogUtils.i("WiFiConnector", "wifiConfiguration!=null");
            wifiConfiguration = updateConfig(wifiConfiguration);
            LogUtils.i("WiFiConnector", "updateConfig(wifiConfiguration)=" + wifiConfiguration);
            try {
                mWifiManager.updateNetwork(wifiConfiguration);//某些系统中，不是自己创建config是无法update的，会抛出IllegalStateException
            } catch (Throwable e) {
                e.printStackTrace();
            }
            LogUtils.i("WiFiConnector", "mWifiManager.updateNetwork(wifiConfiguration)");
            if (mWifiManager.enableNetwork(wifiConfiguration.networkId, true)) {
                LogUtils.i("WiFiConnector", "enableNetWork");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    LogUtils.i("WiFiConnector", "Build.VERSION.SDK_INT >= Build.VERSION_CODES.N");
                    try {
                        mWifiManager.saveConfiguration();
                        LogUtils.i("WiFiConnector", "saveConfiguration()");
                    } catch (Exception e) {//deadsystemexception这里无法catch,这个方式不知道能不能解决，因为这里华为系统8.0以上会出现系统进程崩溃，具体原因无法了解
                        LogUtils.e("WiFiConnector", e.getMessage());
                    }
                } else {
                    LogUtils.i("WiFiConnector", "Build.VERSION.SDK_INT < Build.VERSION_CODES.N");
                    mWifiManager.saveConfiguration();
                    LogUtils.i("WiFiConnector", "saveConfiguration()");
                }
                mWifiManager.reconnect();
                LogUtils.i("WiFiConnector", "enableNetwork true" + wifiConfiguration.networkId);
                return wifiConfiguration.networkId;
            } else {
                LogUtils.i("WiFiConnector", "enableNetwork false" + wifiConfiguration.networkId);
                return -1;
            }
        }
    }

    /**
     * 通过反射调用系统的API进行网络连接。
     *
     * @param configuration 配置
     * @return int-networkID
     */
    private int connectWifiReflect(WifiConfiguration configuration, WifiConfiguration existConfig) {
        LogUtils.i("WifiMonitor", "Connect reflect wifi: " + configuration);
        int networkID = configuration.networkId;
        try {
            if (existConfig != null) {
                networkID = existConfig.networkId;
                mConnectMethod2.invoke(mWifiManager, existConfig.networkId, null);
                LogUtils.i("WiFiConnector", "has existconfig" + existConfig);
            } else {
                mConnectMethod.invoke(mWifiManager, configuration, null);
                //让上层等待连接
                networkID = 1000;
                LogUtils.i("WiFiConnector", "has no config" + configuration);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return connect();
        }


        return networkID;
    }


    private WifiConfiguration getExistedConfiguration(String SSID) {
        try {
            List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
            if (existingConfigs == null) return null;
            String ssid = convertToQuotedString(SSID);
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (!TextUtils.isEmpty(existingConfig.SSID) && existingConfig.SSID.equals(ssid)) {
                    return existingConfig;
                }
            }
        } catch (Throwable e) {//有的机型会抛 android.os.DeadSystemException
            e.printStackTrace();
        }

        return null;
    }

    /***
     * 判断wifi配置是否disable状态
     * @param wifiConfiguration
     * @return
     */
    private boolean isDiasble(WifiConfiguration wifiConfiguration) {
        Class<?> clazz = wifiConfiguration.getClass();
        try {
            Object object = ReflectUtil.getField(clazz, wifiConfiguration, "mNetworkSelectionStatus");
            String status = (String) ReflectUtil.invoke(object.getClass(), wifiConfiguration, "getNetworkStatusString", object);
            return "NETWORK_SELECTION_TEMPORARY_DISABLE".equalsIgnoreCase(status) ||
                    "NETWORK_SELECTION_PERMANENTLY_DISABLE".equalsIgnoreCase(status);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void removeExistConfig(String ssid) {
        List<WifiConfiguration> wifiConfigurationList = mWifiManager.getConfiguredNetworks();
        ssid = parseSSID(ssid);
        for (WifiConfiguration cfg : wifiConfigurationList) {
            if (ssid.equalsIgnoreCase(cfg.SSID)) {
                mWifiManager.removeNetwork(cfg.networkId);
            }
        }
    }

    private String parseSSID(String ssid) {
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            return ssid;
        } else {
            return String.format("\"%s\"", ssid);
        }
    }

    private WifiConfiguration getConfig() {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        if (mScanResult == null) {
            config.SSID = convertToQuotedString(mSSID);
            config.hiddenSSID = isHidden;
        } else {
            config.SSID = convertToQuotedString(mScanResult.SSID);
        }
        int security = getSecurity(mCapabilities);
        switch (security) {
            case SECURITY_NONE: {
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                break;
            }
            case SECURITY_WEP: {
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (mPassword.length() != 0) {
                    int length = mPassword.length();
                    String password = mPassword;
                    if ((length == 10 || length == 26 || length == 58)
                            && password.matches("[0-9A-Fa-f]{64}")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                break;
            }
            case SECURITY_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.status = WifiConfiguration.Status.ENABLED;
                if (mPassword.length() != 0) {
                    String password = mPassword;
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;
            case SECURITY_EAP: {
                throw new RuntimeException("EAP network not support.");
            }
            default:
                return null;
        }
        return config;
    }

    private WifiConfiguration updateConfig(WifiConfiguration config) {
        if (mScanResult == null) {
            config.SSID = convertToQuotedString(mSSID);
            config.hiddenSSID = isHidden;
        } else {
            config.SSID = convertToQuotedString(mScanResult.SSID);
        }
        int security = getSecurity(mCapabilities);
        switch (security) {
            case SECURITY_NONE: {
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                break;
            }
            case SECURITY_WEP: {
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (mPassword.length() != 0) {
                    int length = mPassword.length();
                    String password = mPassword;
                    if ((length == 10 || length == 26 || length == 58)
                            && password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                break;
            }
            case SECURITY_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (mPassword.length() != 0) {
                    String password = mPassword;
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;
            case SECURITY_EAP: {
                throw new RuntimeException("EAP network not support.");
            }
        }
        return config;
    }

    public void setNetworkCallback(ConnectivityManager.NetworkCallback networkCallback) {
        this.mNetworkCallback = networkCallback;
    }

    public void setSSID(String mSSID) {
        this.mSSID = mSSID;
    }

    public void setCapabilities(String mCapabilities) {
        this.mCapabilities = mCapabilities;
    }

    public void setPassword(String mPassword) {
        this.mPassword = mPassword;
    }

    public String getCapabilities() {
        return mCapabilities;
    }

    public String getPassword() {
        return mPassword;
    }
}
