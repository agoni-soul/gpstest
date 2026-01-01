package com.soul.network;

import static com.soul.network.NetworkMonitor.EXTRA_NETWORK_STATE;
import static com.soul.network.NetworkMonitor.EXTRA_PRE_NETWORK_INFO;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

/**
 * Ethernet monitor.
 * <p>For Ethernet app can monitor the connect state by receive the broadcast {@link #ACTION_ETHERNET_STATE_CHANGED}.
 */

public class EthernetNetworkMonitor extends NetStateMachine {

    private static final String TAG = "EthernetNetworkMonitor";

    /**
     * Ethernet network connect state changed.
     * Get ethernet connect state by broadcast intent#getBooleanExtra({@link NetworkMonitor#EXTRA_NETWORK_STATE},false).<br/>
     * If current ethernet network is connected,app can get current ethernet network info by intent#getParcelableExtra({@link NetworkMonitor#EXTRA_NETWORK_STATE}),<br/>
     * if current ethernet network is disconnected and previous state is connected,
     * app can also get the previous ethernet network info by intent#getParcelableExtra({@link NetworkMonitor#EXTRA_PRE_NETWORK_INFO})
     * and will return null if previous state is disconnected.<br/>
     *
     * @see NetworkMonitor#EXTRA_NETWORK_STATE
     * @see NetworkMonitor#EXTRA_NETWORK_INFO
     * @see NetworkMonitor#EXTRA_PRE_NETWORK_INFO
     */
    public static final String ACTION_ETHERNET_STATE_CHANGED = "com.seagle.android.net.monitor.ACTION_ETHERNET_STATE_CHANGED";

    EthernetNetworkMonitor(Context context) {
        super(context);
    }

    @Override
    protected void notifyNetworkState(boolean connected, NetworkInfo networkInfo) {
        if (connected) {
            mNetworkInfo = networkInfo;
            Intent broadCastIntent = new Intent(ACTION_ETHERNET_STATE_CHANGED);
            broadCastIntent.putExtra(EXTRA_NETWORK_STATE, true);
            broadCastIntent.putExtra(NetworkMonitor.EXTRA_NETWORK_INFO, mNetworkInfo);
            mContext.sendStickyBroadcast(broadCastIntent);
            Log.i(TAG, "Ethernet network connected: " + mNetworkInfo);
        } else {
            Log.i(TAG, "Ethernet network disconnected: " + mNetworkInfo);
            Intent broadCastIntent = new Intent(ACTION_ETHERNET_STATE_CHANGED);
            broadCastIntent.putExtra(EXTRA_NETWORK_STATE, false);
            if (mNetworkInfo != null) {
                broadCastIntent.putExtra(EXTRA_PRE_NETWORK_INFO, mNetworkInfo);
            }
            mContext.sendStickyBroadcast(broadCastIntent);
            mNetworkInfo = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected NetworkRequest getNetRequest() {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET);
        return builder.build();
    }

    @Override
    void stop() {
        if (mContext != null) {
            Intent broadCastIntent = new Intent(ACTION_ETHERNET_STATE_CHANGED);
            mContext.removeStickyBroadcast(broadCastIntent);
        }
        super.stop();
    }
}
