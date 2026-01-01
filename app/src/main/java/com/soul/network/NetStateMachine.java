package com.soul.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

/**
 * The parent class of network state monitor.
 */
abstract class NetStateMachine {
    private static final String TAG="NetStateMachine";
    volatile NetworkInfo mNetworkInfo;
    volatile Network mNetwork;
    private ConnectivityManager.NetworkCallback mNetCallback;
    private ConnectivityManager mConnectivityManager;
    Context mContext;

    NetStateMachine(Context context) {
        mContext = context;
        mConnectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    void setNetworkInfo(NetworkInfo networkInfo) {
        mNetworkInfo = networkInfo;
    }

    /**
     * Start network state monitor
     */
    void start() {
        if (mNetworkInfo == null || mNetworkInfo.getState() != NetworkInfo.State.CONNECTED) {
            notifyNetworkState(false, null);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mNetCallback = new ConnectivityManager.NetworkCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onAvailable(Network network) {
                    mNetwork = network;
                    if (network != null) {
                        NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(network);
                        Log.i(TAG, "xxxxonAvailable:" + networkInfo);
                        notifyNetworkState(true, networkInfo);
                    }else{
                        Log.i(TAG, "xxxxonAvailable:null");
                    }
                }

                @Override
                public void onLost(Network network) {
                    mNetwork = network;
                    Log.i(TAG,"xxxxononLost:"+mNetworkInfo);
                    notifyNetworkState(false, null);
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    Log.i(TAG,"onUnavailable:"+mNetworkInfo);
                    notifyNetworkConnectFail();
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    Log.i(TAG,"onCapabilitiesChanged("+(network== null?"null":mConnectivityManager.getNetworkInfo(network))+","+networkCapabilities);
                    if(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)){
                        if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                            Log.e(TAG, "wifi网络已连接");
                        }else {
                            Log.e(TAG, "移动网络已连接");
                        }
                    }
                }
            };
            mConnectivityManager.registerNetworkCallback(getNetRequest(), mNetCallback);
        }
    }

    /**
     * Stop network state monitor.
     * Do some thing release.
     */
    void stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mConnectivityManager.unregisterNetworkCallback(mNetCallback);
        }
        mContext = null;
        mNetworkInfo = null;
        mNetwork = null;
    }

    /**
     * Return is connected.
     *
     * @return network is connected
     */
    public boolean isConnected() {
        NetworkInfo networkInfo = mNetworkInfo;
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Return network info or null.
     *
     * @return NetworkInfo
     */
    public NetworkInfo getNetworkInfo() {
        return mNetworkInfo;
    }

    /**
     * Return network.
     * If wifi not connected or below android LOLLIPOP version return null.
     *
     * @return Network
     */
    public Network getNetwork() {
        return mNetwork;
    }

    /**
     * Return NetworkRequest.
     *
     * @return NetworkRequest
     */
    protected abstract NetworkRequest getNetRequest();

    /**
     * Notify the network state changed.
     *
     * @param connected   Is network connected
     * @param networkInfo NetworkInfo
     */
    protected abstract void notifyNetworkState(boolean connected, NetworkInfo networkInfo);

    protected void notifyNetworkConnectFail(){

    }

}
