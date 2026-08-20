package com.haha.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Android system network monitor.
 */
public class NetworkMonitor {

    private static final String TAG = "SENetworkMonitor";

    /**
     * Network connect state changed.
     * Get connect state by broadcast intent#getBooleanExtra({@link #EXTRA_NETWORK_STATE},false).<br/>
     * If current system network is connected,app can get current network info by intent#getParcelableExtra({@link #EXTRA_NETWORK_STATE}),<br/>
     * if current system network is disconnected and previous state is connected,
     * app can also get the previous network info by intent#getParcelableExtra({@link #EXTRA_PRE_NETWORK_INFO})
     * and will return null if previous state is disconnected.<br/>
     *
     * @see #EXTRA_NETWORK_STATE
     * @see #EXTRA_NETWORK_INFO
     * @see #EXTRA_PRE_NETWORK_INFO
     */
    public static final String ACTION_NETWORK_STATE_CHANGED = "com.seagle.android.net.monitor.ACTION_NETWORK_STATE_CHANGED";

    /**
     * Network type changed.
     * Means network type changed,such as network changed form wifi to mobile,or from mobile to wifi,or form ethernet to wifi etc.
     * app can get previous network info by extra {@link #EXTRA_PRE_NETWORK_INFO} and get current network info by extra {@link #EXTRA_NETWORK_INFO}.<br>
     * When receive this broadcast notification,app will also receive a network connected notification
     * if app register the {@link #ACTION_NETWORK_STATE_CHANGED} broadcast receiver.
     *
     * @see #EXTRA_NETWORK_INFO
     * @see #EXTRA_PRE_NETWORK_INFO
     */
    public static final String ACTION_NETWORK_TYPE_CHANGED = "com.seagle.android.net.monitor.ACTION_NETWORK_TYPE_CHANGED";

    /**
     * Network connect state change broad action extra key.
     *
     * @see #ACTION_NETWORK_STATE_CHANGED
     */
    public static final String EXTRA_NETWORK_STATE = "networkState";

    /**
     * Network info extras key.
     *
     * @see #ACTION_NETWORK_STATE_CHANGED
     * @see #ACTION_NETWORK_TYPE_CHANGED
     */
    public static final String EXTRA_NETWORK_INFO = "networkInfo";

    /**
     * The previous Network info extras.
     *
     * @see #ACTION_NETWORK_STATE_CHANGED
     * @see #ACTION_NETWORK_TYPE_CHANGED
     */
    public static final String EXTRA_PRE_NETWORK_INFO = "preNetworkInfo";

    private SoftReference<Context> mContext;
    private volatile boolean mStarted;
    private volatile NetworkInfo mActiveNetworkInfo;
    private ConnectivityManager mConnectivityManager;
    private final ConnectionChangeReceiver mConnectionChangeReceiver;
    private WiFiNetworkMonitor mWifiStateMachine;
    private MobileNetworkMonitor mMobileStateMachine;
    private EthernetNetworkMonitor mEthernetStateMachine;

    private static NetworkMonitor sInstance;

    public synchronized static NetworkMonitor getInstance() {
        if (sInstance == null) {
            sInstance = new NetworkMonitor();
        }
        return sInstance;
    }

    private NetworkMonitor() {
        mConnectionChangeReceiver = new ConnectionChangeReceiver();
    }

    /**
     * Start monitor network state.
     *
     * @param context Android Context
     */
    public synchronized void startMonitoring(Context context) {
        if (!mStarted) {
            if (context == null) {
                throw new IllegalArgumentException("Context should not be null!");
            }

            mWifiStateMachine = new WiFiNetworkMonitor(context.getApplicationContext());
            mMobileStateMachine = new MobileNetworkMonitor(context.getApplicationContext());
            mEthernetStateMachine = new EthernetNetworkMonitor(context.getApplicationContext());

            mStarted = true;
            mContext = new SoftReference<>(context.getApplicationContext());
            mConnectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(mConnectionChangeReceiver, filter);
            if (mConnectivityManager == null) {
                throw new NullPointerException("Get system connectivity service failed!");
            }
            mActiveNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mActiveNetworkInfo == null || mActiveNetworkInfo.getState() != NetworkInfo.State.CONNECTED) {
                notifyNetworkDisconnected(context, mActiveNetworkInfo);
            } else {
                notifyNetworkConnected(context, mActiveNetworkInfo);
            }
            initNetwork();
            mWifiStateMachine.start();
            mMobileStateMachine.start();
            mEthernetStateMachine.start();
        }
    }


    /**
     * Stop monitor network.
     * Do some thing release.
     */
    public synchronized void stopMonitoring() {
        if (mStarted) {
            mStarted = false;
            if (mContext != null) {
                Context context = mContext.get();
                if (context != null) {
                    Intent broadCastIntent = new Intent(ACTION_NETWORK_STATE_CHANGED);
                    context.removeStickyBroadcast(broadCastIntent);
                    context.unregisterReceiver(mConnectionChangeReceiver);
                }
            }
            mWifiStateMachine.stop();
            mMobileStateMachine.stop();
            mEthernetStateMachine.stop();
            mActiveNetworkInfo = null;
        }
    }

    /**
     * Return WiFiNetworkMonitor.
     *
     * @return WiFiNetworkMonitor
     */
    public WiFiNetworkMonitor getWiFiNetworkMonitor() {
        return mWifiStateMachine;
    }

    /**
     * Return MobileNetworkMonitor.
     *
     * @return MobileNetworkMonitor
     */
    public MobileNetworkMonitor getMobileNetworkMonitor() {
        return mMobileStateMachine;
    }

    /**
     * Return EthernetNetworkMonitor.
     *
     * @return EthernetNetworkMonitor
     */
    public EthernetNetworkMonitor getEthernetNetworkMonitor() {
        return mEthernetStateMachine;
    }

    /**
     * Return ConnectivityManager.
     *
     * @return ConnectivityManager
     */
    public ConnectivityManager getConnectivityManager() {
        return mConnectivityManager;
    }

    /**
     * Current system network is connected.
     *
     * @return network connected state.
     */
    public boolean isConnected() {
        return (mActiveNetworkInfo != null && mActiveNetworkInfo.isConnected());
    }

    /**
     * Return current network info.
     * Return null if not connected.
     *
     * @return NetworkInfo
     */
    public NetworkInfo getNetworkInfo() {
        return mActiveNetworkInfo;
    }

    /**
     * Notify network disconnected.
     *
     * @param context        context
     * @param preNetworkInfo previous network info
     */
    private void notifyNetworkDisconnected(Context context, NetworkInfo preNetworkInfo) {
        Intent broadCastIntent = new Intent(ACTION_NETWORK_STATE_CHANGED);
        broadCastIntent.putExtra(EXTRA_NETWORK_STATE, false);
        broadCastIntent.putExtra(EXTRA_PRE_NETWORK_INFO, preNetworkInfo);
        context.sendStickyBroadcast(broadCastIntent);
    }

    /**
     * Notify network connected.
     *
     * @param context        context
     * @param curNetworkInfo current network info
     */
    private void notifyNetworkConnected(Context context, NetworkInfo curNetworkInfo) {
        Intent broadCastIntent = new Intent(ACTION_NETWORK_STATE_CHANGED);
        broadCastIntent.putExtra(EXTRA_NETWORK_STATE, true);
        broadCastIntent.putExtra(EXTRA_NETWORK_INFO, curNetworkInfo);
        context.sendStickyBroadcast(broadCastIntent);
    }

    /**
     * Notify network changed.
     *
     * @param context        context
     * @param preNetworkInfo previous network info
     * @param curNetworkInfo current network info
     */
    private void notifyNetworkChanged(Context context, NetworkInfo preNetworkInfo, NetworkInfo curNetworkInfo) {
        Intent broadCastIntent = new Intent(ACTION_NETWORK_TYPE_CHANGED);
        broadCastIntent.putExtra(EXTRA_PRE_NETWORK_INFO, preNetworkInfo);
        broadCastIntent.putExtra(EXTRA_NETWORK_INFO, curNetworkInfo);
        broadCastIntent.addFlags(0x01000000);
        context.sendBroadcast(broadCastIntent);

        Intent intent = new Intent(ACTION_NETWORK_STATE_CHANGED);
        intent.putExtra(EXTRA_NETWORK_STATE, true);
        intent.putExtra(EXTRA_NETWORK_INFO, curNetworkInfo);
        context.sendStickyBroadcast(intent);
    }

    /**
     * Init network status.
     */
    private void initNetwork() {
        List<NetworkInfo> networkInfoList = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = mConnectivityManager.getAllNetworks();
            for (Network network : networks) {
                NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(network);
                if (networkInfo != null) {
                    networkInfoList.add(networkInfo);
                }
            }
        } else {
            NetworkInfo[] networkInfoArray = mConnectivityManager.getAllNetworkInfo();
            if (networkInfoArray != null && networkInfoArray.length > 0) {
                networkInfoList.addAll(Arrays.asList(networkInfoArray));
            }
        }
        for (NetworkInfo networkInfo : networkInfoList) {
            if (ConnectivityManager.TYPE_WIFI == networkInfo.getType()) {
                mWifiStateMachine.setNetworkInfo(networkInfo);
            } else if (ConnectivityManager.TYPE_MOBILE == networkInfo.getType()) {
                mMobileStateMachine.setNetworkInfo(networkInfo);
            } else if (ConnectivityManager.TYPE_ETHERNET == networkInfo.getType()) {
                mEthernetStateMachine.setNetworkInfo(networkInfo);
            }
        }
    }

    /**
     * The system connection change broadcast receiver.
     */
    class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo activeNetworkInfo = null;
            try{
                activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            }catch (Throwable throwable){//一些特殊手机系统会出现android.os.DeadSystemException
                throwable.printStackTrace();
            }

            //解决NetworkInfo[type: WIFI[], state: DISCONNECTED/BLOCKED, reason: (unspecified), extra: (none), failover: false, available: true, roaming: false]
            //复现步骤应该是应用到后台之后，切换wifi，还有一些其他必须原因，暂时复现不出来
            NetworkInfo intentNetworkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (intentNetworkInfo == null) {
                intentNetworkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            }
            if (activeNetworkInfo == null) {
                activeNetworkInfo = intentNetworkInfo;
            } else {
                //防止收到activeNetworkInfo的状态是 DISCONNECTED/BLOCKED ,但是Intent中的是CONNECTED/CONNECTED
                if (intentNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting() != intentNetworkInfo.isConnectedOrConnecting()) {
                    activeNetworkInfo = intentNetworkInfo;
                }
            }
            Log.d("xxxxonReceive", activeNetworkInfo == null ? "null" : activeNetworkInfo.toString());
            if (activeNetworkInfo == null) {
                if (mActiveNetworkInfo != null) {
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                        mWifiStateMachine.notifyNetworkState(false, null);
                        mMobileStateMachine.notifyNetworkState(false, null);
                        mEthernetStateMachine.notifyNetworkState(false, null);
                    }
                    notifyNetworkDisconnected(context, mActiveNetworkInfo);
                    mActiveNetworkInfo = null;
                    Log.i(TAG, "Network disconnected!");
                }
            } else if (mActiveNetworkInfo == null) {
                Log.i(TAG, "Network connected: " + activeNetworkInfo);
                if (ConnectivityManager.TYPE_MOBILE == activeNetworkInfo.getType()) {
                    mActiveNetworkInfo = activeNetworkInfo;
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                        mMobileStateMachine.notifyNetworkState(true, mActiveNetworkInfo);
                    }
                    notifyNetworkConnected(context, mActiveNetworkInfo);
                } else if (ConnectivityManager.TYPE_WIFI == activeNetworkInfo.getType()) {
                    mActiveNetworkInfo = activeNetworkInfo;
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                        mWifiStateMachine.notifyNetworkState(true, mActiveNetworkInfo);
                    }
                    notifyNetworkConnected(context, mActiveNetworkInfo);
                } else if (ConnectivityManager.TYPE_ETHERNET == activeNetworkInfo.getType()) {
                    mActiveNetworkInfo = activeNetworkInfo;
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                        mEthernetStateMachine.notifyNetworkState(true, mActiveNetworkInfo);
                    }
                    notifyNetworkConnected(context, mActiveNetworkInfo);
                } else {
                    Log.i(TAG, "Other Network connected!");
                    mActiveNetworkInfo = null;
                }
            } else if (mActiveNetworkInfo.getType() != activeNetworkInfo.getType()) {
                if (ConnectivityManager.TYPE_MOBILE == activeNetworkInfo.getType()) {
                    Log.i(TAG, "Network change to mobile: " + activeNetworkInfo);
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                        mMobileStateMachine.notifyNetworkState(true, activeNetworkInfo);
                        mWifiStateMachine.notifyNetworkState(false, null);
                        mEthernetStateMachine.notifyNetworkState(false, null);
                    }
                    NetworkInfo preNetworkInfo = mActiveNetworkInfo;
                    mActiveNetworkInfo = activeNetworkInfo;
                    notifyNetworkChanged(context, preNetworkInfo, mActiveNetworkInfo);
                } else if (ConnectivityManager.TYPE_WIFI == activeNetworkInfo.getType()) {
                    Log.i(TAG, "Network change to wifi: " + activeNetworkInfo);
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                        mWifiStateMachine.notifyNetworkState(true, activeNetworkInfo);
                        mMobileStateMachine.notifyNetworkState(false, null);
                        mEthernetStateMachine.notifyNetworkState(false, null);
                    }
                    NetworkInfo preNetworkInfo = mActiveNetworkInfo;
                    mActiveNetworkInfo = activeNetworkInfo;
                    notifyNetworkChanged(context, preNetworkInfo, mActiveNetworkInfo);
                } else if (ConnectivityManager.TYPE_ETHERNET == activeNetworkInfo.getType()) {
                    Log.i(TAG, " Network change to ethernet: " + activeNetworkInfo);
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                        mEthernetStateMachine.notifyNetworkState(true, activeNetworkInfo);
                        mWifiStateMachine.notifyNetworkState(false, null);
                        mMobileStateMachine.notifyNetworkState(false, null);
                    }
                    NetworkInfo preNetworkInfo = mActiveNetworkInfo;
                    mActiveNetworkInfo = activeNetworkInfo;
                    notifyNetworkChanged(context, preNetworkInfo, mActiveNetworkInfo);
                } else {
                    Log.i(TAG, "Other Network connected!");
                    mActiveNetworkInfo = null;
                }
            }
        }
    }
}
