package com.haha.network;

import static com.haha.network.NetworkMonitor.EXTRA_NETWORK_INFO;
import static com.haha.network.NetworkMonitor.EXTRA_NETWORK_STATE;
import static com.haha.network.NetworkMonitor.EXTRA_PRE_NETWORK_INFO;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;


/**
 * Mobile monitor.
 * <p>For Mobile network app can monitor the connect state by receive the broadcast {@link #ACTION_MOBILE_STATE_CHANGED}.
 */

public class MobileNetworkMonitor extends NetStateMachine {

    private static final String TAG = "MobileNetworkMonitor";

    /**
     * Mobile network connect state changed.
     * Get mobile connect state by broadcast intent#getBooleanExtra({@link NetworkMonitor#EXTRA_NETWORK_STATE},false).<br/>
     * If current mobile network is connected,app can get current mobile network info by intent#getParcelableExtra({@link NetworkMonitor#EXTRA_NETWORK_STATE}),<br/>
     * if current mobile network is disconnected and previous state is connected,
     * app can also get the previous mobile network info by intent#getParcelableExtra({@link NetworkMonitor#EXTRA_PRE_NETWORK_INFO})
     * and will return null if previous state is disconnected.<br/>
     *
     * @see NetworkMonitor#EXTRA_NETWORK_STATE
     * @see NetworkMonitor#EXTRA_NETWORK_INFO
     * @see NetworkMonitor#EXTRA_PRE_NETWORK_INFO
     */
    public static final String ACTION_MOBILE_STATE_CHANGED = "com.seagle.android.net.monitor.ACTION_MOBILE_STATE_CONNECTED";

    private volatile TelephonyManager mTelephonyManager;

    MobileNetworkMonitor(Context context) {
        super(context);
    }

    /**
     * Return TelephonyManager.
     *
     * @return TelephonyManager
     */
    public TelephonyManager getTelephonyManager() {
        if (mTelephonyManager == null) {
            synchronized (this) {
                if (mTelephonyManager == null) {
                    mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                }
            }
        }
        return mTelephonyManager;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected NetworkRequest getNetRequest() {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        return builder.build();
    }

    @Override
    protected void notifyNetworkState(boolean connected, NetworkInfo networkInfo) {
        if (connected) {
            mNetworkInfo = networkInfo;
            Intent broadCastIntent = new Intent(ACTION_MOBILE_STATE_CHANGED);
            broadCastIntent.putExtra(EXTRA_NETWORK_STATE, true);
            broadCastIntent.putExtra(EXTRA_NETWORK_INFO, mNetworkInfo);
            mContext.sendStickyBroadcast(broadCastIntent);
            Log.i(TAG, "Mobile network connected: " + mNetworkInfo);
        } else {
            Log.i(TAG, "Mobile network disconnected: " + mNetworkInfo);
            Intent broadCastIntent = new Intent(ACTION_MOBILE_STATE_CHANGED);
            broadCastIntent.putExtra(EXTRA_NETWORK_STATE, false);
            if (mNetworkInfo != null) {
                broadCastIntent.putExtra(EXTRA_PRE_NETWORK_INFO, mNetworkInfo);
            }
            mContext.sendStickyBroadcast(broadCastIntent);
            mNetworkInfo = null;
        }
    }

    @Override
    void stop() {
        if (mContext != null) {
            Intent broadCastIntent = new Intent(ACTION_MOBILE_STATE_CHANGED);
            mContext.removeStickyBroadcast(broadCastIntent);
        }
        super.stop();
    }
}
