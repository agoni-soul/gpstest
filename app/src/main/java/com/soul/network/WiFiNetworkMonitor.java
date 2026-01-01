package com.soul.network;

import static com.soul.network.NetworkMonitor.EXTRA_NETWORK_INFO;
import static com.soul.network.NetworkMonitor.EXTRA_NETWORK_STATE;
import static com.soul.network.NetworkMonitor.EXTRA_PRE_NETWORK_INFO;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * WiFi monitor.
 */
public class WiFiNetworkMonitor extends NetStateMachine {

    private static final String TAG = "WiFiNetworkMonitor";

    /**
     * WiFi network connect state changed.
     * Get WiFi connect state by broadcast intent#getBooleanExtra({@link NetworkMonitor#EXTRA_NETWORK_STATE},false).<br/>
     * If current WiFi network is connected,app can get current network info by intent#getParcelableExtra({@link NetworkMonitor#EXTRA_NETWORK_STATE}),
     * and get wifi info by intent#getParcelableExtra({@link #EXTRA_WIFI_INFO}).<br/>
     * if current WiFi network is disconnected and previous state is connected,
     * app can also get the previous network info by intent#getParcelableExtra({@link NetworkMonitor#EXTRA_PRE_NETWORK_INFO})
     * and will return null if previous state is disconnected.<br/>
     *
     * @see NetworkMonitor#EXTRA_NETWORK_STATE
     * @see NetworkMonitor#EXTRA_NETWORK_INFO
     * @see NetworkMonitor#EXTRA_PRE_NETWORK_INFO
     * @see #EXTRA_WIFI_INFO
     */
    public static final String ACTION_WIFI_STATE_CHANGED = "com.midea.iot.msmart.network.ACTION_WIFI_STATE_CONNECTED";

    public static final String ACTION_WIFI_STATE_CONNECT_FAIL = "com.midea.iot.msmart.network.ACTION_WIFI_STATE_CONNECT_FAIL";

    /**
     * WiFi info extras key.
     */
    public static final String EXTRA_WIFI_INFO = "wifiInfo";

    /**
     * Connect WiFi success.
     */
    public static final int CONNECT_SUCCESS = 0;

    /**
     * Connect WiFi error code: other err.
     */
    public static final int ERR_CONNECT_FAILED = -1;

    /**
     * Connect WiFi error code: timeout.
     */
    public static final int ERR_CONNECT_TIMEOUT = -2;

    /**
     * Connect WiFi error code: password wrong.
     */
    public static final int ERR_PASSWORD_WRONG = -3;

    private static final String UNKNOW_SSID = "<unknown ssid>";

    /**
     * Capabilities:WEP.
     * For WEP or OPEN access point.
     */
    private static final String SECURITY_WEP = "WEP";

    /**
     * Capabilities:PSK.
     * For WAP-PSK or WAP2-PSK access point.
     */
    private static final String SECURITY_PSK = "PSK";

    private volatile WifiInfo mWifiInfo;
    private WifiManager mWifiManager;
    private ConnectivityManager mConnectivityManager;
    //检查wifi的线程池
    private ExecutorService mCheckWifiInofPool;

    private WiFiConnector mConnector;


    WiFiNetworkMonitor(Context context) {
        super(context);
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
        mCheckWifiInofPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2 + 4, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "SLK_Task_ThreadPool");
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    /**
     * Return current WiFi info.
     * Return null if WiFi not connected.
     *
     * @return WifiInfo
     */
    public WifiInfo getWiFiInfo() {
        if (isWiFiEnabled())
            mWifiInfo = mWifiManager.getConnectionInfo();
        return mWifiInfo;
    }

    /**
     * Enable WiFi.
     *
     * @return operation result
     */
    public boolean enableWiFi() {
        try {
            return mWifiManager.setWifiEnabled(true);
        }catch (Exception e){//没权限，或者一些特殊手机的权限
            e.printStackTrace();
        }
        return false;

    }

    /**
     * Disable WiFi.
     *
     * @return operation result
     */
    public boolean disableWiFi() {
        return mWifiManager.setWifiEnabled(false);
    }

    /**
     * Return WiFi enable state.
     *
     * @return WiFi enable state
     */
    public boolean isWiFiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    /**
     * Connect WiFi.
     * Support sync and async call method: <br>
     * If callback is null,will block the call thread and return result by synchronous.<br>
     * If callback not null,this method return -1 immediately and the connection result return by callback.
     * <p>
     * Attention:not support connect new EAP network.
     *
     * @param result   WiFi ScanResult
     * @param password WiFi password if needed
     * @param callback Connect result callback
     * @return Connect result
     */
    @Deprecated
    public int connectWiFi(ScanResult result, String password, final WiFiConnectCallback callback) {
        final WiFiConnector connector = new WiFiConnector(result, password, mWifiManager);
        final ConnectWiFiTask task = new ConnectWiFiTask(connector);
        if (callback == null) {
            return task.call();
        } else {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int result = task.call();
                    if (result == CONNECT_SUCCESS) {
                        callback.onConnectSuccess();
                    } else {
                        String message = "Connect WiFi " + connector.getSSID() + " failed as unknown err!";
                        if (ERR_CONNECT_TIMEOUT == result) {
                            message = "Connect WiFi " + connector.getSSID() + " timeout!";
                        } else if (ERR_PASSWORD_WRONG == result) {
                            message = "Connect WiFi " + connector.getSSID() + " failed as password wrong!";
                        }
                        callback.onConnectFailed(result, message);
                    }
                }
            });
            thread.start();
            return -1;
        }
    }

    /**
     * Connect WiFi.
     * Support sync and async call method: <br>
     * If callback is null,will block the call thread and return result by synchronous.<br>
     * If callback not null,this method return -1 immediately and the connection result return by callback.
     * <p>
     * Attention:not support connect new EAP network.
     *
     * @param ssid         WiFi SSID
     * @param capabilities WiFi capabilities
     * @param password     WiFi password if needed
     * @param callback     Connect result callback
     * @return Connect result
     */
    public int connectWiFi(String ssid, String capabilities, String password, final WiFiConnectCallback callback) {
        return connectWiFi(ssid, capabilities, password, false, callback);
    }

    /**
     * Connect WiFi.
     * Support sync and async call method: <br>
     * If callback is null,will block the call thread and return result by synchronous.<br>
     * If callback not null,this method return -1 immediately and the connection result return by callback.
     * <p>
     * Attention:not support connect new EAP network.
     *
     * @param ssid         WiFi SSID
     * @param capabilities WiFi capabilities
     * @param password     WiFi password if needed
     * @param callback     Connect result callback
     * @return Connect result
     */
    public int connectWiFi(String ssid, String capabilities, String password, boolean isHidden, final WiFiConnectCallback callback) {
        mConnector = new WiFiConnector(ssid, capabilities, password, isHidden, mWifiManager);
        final ConnectWiFiTask task = new ConnectWiFiTask(mConnector);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mConnector.setNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    if (network != null) {
                        NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(network);
                        Log.d(TAG, "" + networkInfo);
                        notifyNetworkState(true, networkInfo);
                    } else {
                        Log.d(TAG, "null");
                    }
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    notifyNetworkConnectFail();
                }
            });
        }
        if (callback == null) {
            return task.call();
        } else {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int result = task.call();
                    if (result == 0) {
                        callback.onConnectSuccess();
                    } else {
                        String message = "Connect WiFi " + mConnector.getSSID() + " failed as unknown err!";
                        if (ERR_CONNECT_TIMEOUT == result) {
                            message = "Connect WiFi " + mConnector.getSSID() + " timeout!";
                        } else if (ERR_PASSWORD_WRONG == result) {
                            message = "Connect WiFi " + mConnector.getSSID() + " failed as password wrong!";
                        }
                        callback.onConnectFailed(result, message);
                    }
                }
            });
            thread.start();
            return -1;
        }
    }

    /***
     * 释放上一次连接的WIfi，Android Q或以上需要这样处理
     */
    public int releaseWifiConnect(String ssid, String capabilities, String password) {
        if (mConnector != null) {
            mConnector.setSSID(ssid);
            mConnector.setPassword(password);
            mConnector.setCapabilities(capabilities);
        } else {
            mConnector = new WiFiConnector(ssid, capabilities, password, mWifiManager);
        }
        final ConnectWiFiTask task = new ConnectWiFiTask(mConnector);
        task.setReleaseWifiConnect(true);
        return task.call();
    }

    public WifiManager getWifiManager() {
        return mWifiManager;
    }

    @Override
    protected void notifyNetworkState(boolean connected, NetworkInfo networkInfo) {
        if (connected) {
            mNetworkInfo = networkInfo;
            mWifiInfo = mWifiManager.getConnectionInfo();
            Intent broadCastIntent = new Intent(ACTION_WIFI_STATE_CHANGED);
            broadCastIntent.setPackage(mContext.getPackageName());
            broadCastIntent.putExtra(EXTRA_NETWORK_STATE, true);
            broadCastIntent.putExtra(EXTRA_NETWORK_INFO, mNetworkInfo);
            broadCastIntent.putExtra(EXTRA_WIFI_INFO, mWifiInfo);
            mContext.sendBroadcast(broadCastIntent);
            Log.i(TAG, "WiFi network connected: " + mNetworkInfo + "  WiFIInfo:" + mWifiInfo);
//            MSEventCenter.getInstance().dispatchSDKEvent(new MSEvent(MSEventCode.EVENT_CODE_LOGTACKER_ADDTAGS, "LOGTRACKER",
//                    MSKey.KEY_LOGTACKER_ADDTAGS_TRANSACTION, "SLKNETCONFIG",
//                    MSKey.KEY_LOGTACKER_ADDTAGS_KEY, "wifiConnected",
//                    MSKey.KEY_LOGTACKER_ADDTAGS_VALUE, mWifiInfo == null ? "null" : mWifiInfo.getSSID()));
        } else {
            Log.i(TAG, "WiFi network disconnected: " + mNetworkInfo);
//            MSEventCenter.getInstance().dispatchSDKEvent(new MSEvent(MSEventCode.EVENT_CODE_LOGTACKER_ADDTAGS, "LOGTRACKER",
//                    MSKey.KEY_LOGTACKER_ADDTAGS_TRANSACTION, "SLKNETCONFIG",
//                    MSKey.KEY_LOGTACKER_ADDTAGS_KEY, "wifiDisconnected",
//                    MSKey.KEY_LOGTACKER_ADDTAGS_VALUE, mWifiInfo == null ? "null" : mWifiInfo.getSSID()));
            Intent broadCastIntent = new Intent(ACTION_WIFI_STATE_CHANGED);
            broadCastIntent.setPackage(mContext.getPackageName());
            broadCastIntent.putExtra(EXTRA_NETWORK_STATE, false);
            if (mNetworkInfo != null) {
                broadCastIntent.putExtra(EXTRA_PRE_NETWORK_INFO, mNetworkInfo);
                broadCastIntent.putExtra(EXTRA_WIFI_INFO, mWifiInfo);
            }
            mContext.sendBroadcast(broadCastIntent);
            mNetworkInfo = null;
            mNetwork = null;
        }
    }

    /***
     * 连接网络失败
     */
    @Override
    protected void notifyNetworkConnectFail() {
        Log.i(TAG, "连接网络失败了");
        Intent broadCastIntent = new Intent(ACTION_WIFI_STATE_CONNECT_FAIL);
        broadCastIntent.setPackage(mContext.getPackageName());
        broadCastIntent.putExtra(EXTRA_NETWORK_STATE, true);
        broadCastIntent.putExtra(EXTRA_NETWORK_INFO, mNetworkInfo);
        broadCastIntent.putExtra(EXTRA_WIFI_INFO, mWifiInfo);
        mContext.sendBroadcast(broadCastIntent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected NetworkRequest getNetRequest() {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        return builder.build();
    }

    @Override
    void stop() {
        if (mContext != null) {
            Intent broadCastIntent = new Intent(ACTION_WIFI_STATE_CHANGED);
            broadCastIntent.setPackage(mContext.getPackageName());
            mContext.removeStickyBroadcast(broadCastIntent);
        }
        super.stop();
    }

    private class ConnectWiFiTask extends BroadcastReceiver implements Callable<Integer> {
        private static final int S_RETRY_COUNT = 3;
        private WiFiConnector mWiFiConnector;
        private volatile int mNetID = -2;
        private volatile boolean mConnecting;
        private CountDownLatch mLatch;
        private volatile int mResultCode = ERR_CONNECT_FAILED;
        private boolean isCancle;

        private int retryCount;

        private int waitTime;

        //是否是调用释放Wifi的方法，Android Q上使用
        private boolean isReleaseWifiConnect;

        public ConnectWiFiTask(WiFiConnector wiFiConnector) {
            mWiFiConnector = wiFiConnector;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                retryCount = 1;
                waitTime = 30;
            }else{
                retryCount = 3;
                waitTime = 10;
            }
        }

        public void cancle() {
            isCancle = true;
        }

        public void setReleaseWifiConnect(boolean releaseWifiConnect) {
            isReleaseWifiConnect = releaseWifiConnect;
        }

        @Override
        public Integer call() {
            Log.d("releasewifi", "开始了");
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            Log.d(TAG, "wifiInfo = " + wifiInfo);
            Log.d(TAG, "mWiFiConnector.getSSID() = " + mWiFiConnector.getSSID());
            String ssid = WiFiConnector.convertToQuotedString(mWiFiConnector.getSSID());
            Log.d(TAG, "ssid = " + ssid);
            if (wifiInfo != null && ssid.equals(wifiInfo.getSSID())) {
                return CONNECT_SUCCESS;
            }

            //注册广播
            IntentFilter filter = new IntentFilter();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0以上用新的wifi监听方式来处理
                filter.addAction(ACTION_WIFI_STATE_CHANGED);
                filter.addAction(ACTION_WIFI_STATE_CONNECT_FAIL);
            } else {
                filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
            }
            mContext.registerReceiver(this, filter);
            mConnecting = true;
            if (isReleaseWifiConnect) {
                mNetID = 9999;//设置数据，方便路由回切判断
                Log.d("releasewifi", "开始释放999");
                mLatch = new CountDownLatch(1);
                mWiFiConnector.releaseWifiConnector();
                try {
                    if (!mLatch.await(25, TimeUnit.SECONDS)) {
                        Log.d("releasewifi", "释放完成卡住了");
                        checkConnectResult(ssid);
                    } else {
                        Log.d("releasewifi", "释放完成没卡住");
                        checkConnectResult(ssid);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d("releasewifi", "异常了");
                    checkConnectResult(ssid);
                }
            } else {
                for (int i = 0; i < retryCount; i++) {//10秒一次，重试3次，不行就回调
                    mLatch = new CountDownLatch(1);
                    if ((mNetID = mWiFiConnector.connect()) >= 0) {
                        try {
                            if (!mLatch.await(waitTime, TimeUnit.SECONDS)) {
                                Log.i(TAG, "等待释放了" + i);
                                if (checkConnectResult(ssid)) {
                                    break;
                                }
                            } else {
                                Log.i(TAG, "没卡住" + i);
                                if (checkConnectResult(ssid)) {
                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            Log.i(TAG, "异常了" + ex.getMessage());
                            if (checkConnectResult(ssid)) {
                                break;
                            }
                        }
                    }
                    Log.i(TAG, "第一次连接过了，没连上" + i);
                }
            }

            //恢复状态
            mNetID = -2;
            mConnecting = false;
            mContext.unregisterReceiver(this);

            Log.i("WiFiConnector", "resultCode = " + mResultCode);
            cancle();
            return mResultCode;
        }

        /***
         * 校验结果
         */
        private boolean checkConnectResult(String ssid) {
            if (checkConnectWifi(ssid)) {
                Log.i(TAG, "连接成功了");
                mResultCode = CONNECT_SUCCESS;
                return true;
            } else {
                Log.i(TAG, "连接失败了");
                mResultCode = ERR_CONNECT_TIMEOUT;
            }
            return false;
        }

        /***
         * 校验Wi-Fi是否连接正确
         * @return
         */
        private boolean checkConnectWifi(String ssid) {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if (wifiInfo != null && ssid.equalsIgnoreCase(wifiInfo.getSSID())) {
                mResultCode = CONNECT_SUCCESS;
                if (wifiInfo.getIpAddress() != 0) {//分配了ip地址才认为是连接成功
                    return true;
                }
            }
            return false;
        }


        @Override
        public void onReceive(Context context, final Intent intent) {
            Log.d(TAG, "mNetId=" + mNetID + "connectState" + mConnecting + intent.getAction() + intent);
            if (mNetID >= 0 && mConnecting) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0以上用新的wifi监听方式来处理
                    receiverWifiChangeL(intent);
                } else {
                    receiverWifiChange(intent);
                }
            }
        }

        /***
         * 5.0以下的wifi广播处理
         * @param intent
         */
        private void receiverWifiChange(final Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equalsIgnoreCase(action) && mNetID != -2) {
                mCheckWifiInofPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                        Log.i(TAG, "State Changed: " + state);
                        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                        final String ssid = WiFiConnector.convertToQuotedString(mWiFiConnector.getSSID());
                        String wifiName = wifiInfo.getSSID();
                        while (!isCancle) {
                            if (TextUtils.isEmpty(wifiName) || UNKNOW_SSID.equalsIgnoreCase(wifiName)) {//8.0，9.0会出现unknown ssid的情况
                                NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                                if (networkInfo != null)
                                    wifiName = networkInfo.getExtraInfo();
                                if (!TextUtils.isEmpty(wifiName))
                                    wifiName = WiFiConnector.convertToQuotedString(wifiName);
                            } else {
                                break;
                            }
                        }
                        Log.i(TAG, " wifiname= " + wifiName);
                        if (wifiInfo != null && ssid.equalsIgnoreCase(wifiName)) {
                            //防止频繁创建线程
                            SupplicantState wifiState = wifiInfo.getSupplicantState();
                            Log.i(TAG, " xxx wifiState: " + wifiState + "ip=" + getIp(wifiInfo.getIpAddress()));
                            if (SupplicantState.COMPLETED == state && SupplicantState.COMPLETED == wifiState) {
                                if (wifiInfo.getIpAddress() == 0) {//判断Ip分配成功回返回成功,因为有可能刚连上设备的时候，设备分配Ip比较慢
                                    while (!isCancle) {
                                        WifiInfo info = mWifiManager.getConnectionInfo();
                                        if (info != null && info.getIpAddress() != 0) {
                                            Log.i(TAG, "ip 分配完成" + getIp(info.getIpAddress()));
                                            break;
                                        }
                                        Log.i(TAG, "ip没分配");

                                    }
                                } else {
                                    Log.i(TAG, "ip 分配完成" + getIp(wifiInfo.getIpAddress()));
                                }
                                mResultCode = CONNECT_SUCCESS;
                                mWifiManager.saveConfiguration();
                                mLatch.countDown();
                            } else if (SupplicantState.DISCONNECTED == state && SupplicantState.DISCONNECTED == wifiState) {
                                int resultCode = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                                if (resultCode == 1) {
                                    mResultCode = ERR_PASSWORD_WRONG;
                                    mLatch.countDown();
                                } else {
                                    WifiInfo info = mWifiManager.getConnectionInfo();
                                    //可能是有断开的消息时，上面一直循环获取wifi名称，导致以为是当前wifi的断开，所这里也要加个判断
                                    if (info != null && info.getSupplicantState() == SupplicantState.COMPLETED && ssid.equalsIgnoreCase(info.getSSID())) {
                                        mResultCode = CONNECT_SUCCESS;
                                        mWifiManager.saveConfiguration();
                                        mLatch.countDown();
                                        Log.i(TAG, "disconnect 后还连着目标wifi=" + info.getSSID());
                                    } else {
                                        mResultCode = ERR_CONNECT_FAILED;
                                        mLatch.countDown();
                                        mWifiManager.removeNetwork(mNetID);
                                        mWifiManager.reconnect();
                                        Log.i(TAG, "disconnect 后没连着目标wifi=" + ssid);
                                    }

                                }
                            }
                        }
                    }
                });
            }
        }

        /***
         * 5.0以上的wifi广播处理
         * @param intent
         */
        private void receiverWifiChangeL(final Intent intent) {
            String action = intent.getAction();
            if (ACTION_WIFI_STATE_CHANGED.equalsIgnoreCase(action) && mNetID != -2) {
                mCheckWifiInofPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        boolean isConnect = intent.getBooleanExtra(EXTRA_NETWORK_STATE, false);
                        Log.d(TAG, "isConnect=" + isConnect);
                        if (isConnect) {
                            String wifiName = "";
                            final String ssid = WiFiConnector.convertToQuotedString(mWiFiConnector.getSSID());
                            if (mWifiInfo != null) {
                                wifiName = mWifiInfo.getSSID();
                            }
                            if ((TextUtils.isEmpty(wifiName) || UNKNOW_SSID.equalsIgnoreCase(wifiName)) && mNetworkInfo != null) {
                                wifiName = mNetworkInfo.getExtraInfo();
                            }

                            Log.i(TAG, " wifiname= " + wifiName);
                            if (mWifiInfo != null && ssid.equalsIgnoreCase(wifiName)) {
                                if (mWifiInfo.getIpAddress() == 0) {//判断Ip分配成功回返回成功,因为有可能刚连上设备的时候，设备分配Ip比较慢
                                    while (!isCancle) {
                                        WifiInfo info = mWifiManager.getConnectionInfo();
                                        if (info != null && info.getIpAddress() != 0) {
                                            Log.i(TAG, "ip 分配完成" + getIp(info.getIpAddress()));
                                            break;
                                        }
                                        Log.i(TAG, "ip没分配");

                                    }
                                } else {
                                    Log.i(TAG, "ip 分配完成" + getIp(mWifiInfo.getIpAddress()));
                                }
                                mResultCode = CONNECT_SUCCESS;
                                mWifiManager.saveConfiguration();
                                mLatch.countDown();
                            }
                        }
                    }
                });
            } else if (ACTION_WIFI_STATE_CONNECT_FAIL.equalsIgnoreCase(action)) {
                WifiInfo info = mWifiManager.getConnectionInfo();
                final String ssid = WiFiConnector.convertToQuotedString(mWiFiConnector.getSSID());
                if (info != null && info.getSupplicantState() == SupplicantState.COMPLETED && ssid.equalsIgnoreCase(info.getSSID())) {
                    mResultCode = CONNECT_SUCCESS;
                    mWifiManager.saveConfiguration();
                    mLatch.countDown();
                    Log.i(TAG, "底层反馈连接网络失败 后还连着目标wifi=" + info.getSSID());
                } else {
                    mResultCode = ERR_CONNECT_FAILED;
                    mLatch.countDown();
                    mWifiManager.removeNetwork(mNetID);
                    mWifiManager.reconnect();
                    Log.i(TAG, "底层反馈连接网络失败");
                }

            }
        }

        private String getIp(int paramInt) {
            try {
                return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                        + (0xFF & paramInt >> 24);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return String.valueOf(paramInt);
        }
    }

    /**
     * WiFi connect callback.
     */
    public interface WiFiConnectCallback {
        /**
         * Connect success.
         */
        void onConnectSuccess();

        /**
         * Connect failed.
         *
         * @param errorCode    errorCode
         * @param errorMessage errorMessage
         * @see #ERR_CONNECT_FAILED
         * @see #ERR_CONNECT_TIMEOUT
         * @see #ERR_PASSWORD_WRONG
         */
        void onConnectFailed(int errorCode, String errorMessage);
    }
}
