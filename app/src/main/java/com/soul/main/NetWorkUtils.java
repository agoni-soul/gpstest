package com.soul.main;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/06/10
 *     desc   :
 *     version: 1.0
 * </pre>
 */

public class NetWorkUtils {

    public static final String TAG = "haha";
    public static volatile boolean isActivityConnected = true;
    public static volatile boolean isConnected = true;
    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static List<NetworkCapabilities> getAllNetworkCapabilities(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connectivityManager.getAllNetworks();
        List<NetworkCapabilities> list = new ArrayList<>();
        for (Network network : networks) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            list.add(capabilities);
        }
        return list;
    }

    public static String getNetworkName(int transportType) {
        if (transportType == NetworkCapabilities.TRANSPORT_WIFI) {
            return "WIFI";
        } else if (transportType == NetworkCapabilities.TRANSPORT_CELLULAR) {
            return "蜂窝网络";
        } else if (transportType == NetworkCapabilities.TRANSPORT_ETHERNET) {
            return "以太网";
        } else {
            return "其他网络";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static int getTransportType(NetworkCapabilities networkCapabilities) {
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return NetworkCapabilities.TRANSPORT_WIFI;
            //post(NetType.WIFI);
        } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return NetworkCapabilities.TRANSPORT_CELLULAR;
            //post(NetType.CMWAP);
        } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return NetworkCapabilities.TRANSPORT_ETHERNET;
            //post(NetType.AUTO);
        }
        return -1;
    }

    //以太网连上但不能上外网时，程序已经绑定了蜂窝网通道上网，此方法判断是否实际使用蜂窝网上网
    public static boolean isRealCellular(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean activityCellular = isActivityCellular(context);
            if (activityCellular) {
                return true;
            }
            return !isActivityConnected;
        }
        return true;
    }

    //判断激活的网（默认网络）是否4G网
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isActivityCellular(Context context) {
        boolean isCellular = (getTransportType(context) == NetworkCapabilities.TRANSPORT_CELLULAR);
        return isCellular;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean isCellular(int transportType) {
        boolean isCellular = (transportType == NetworkCapabilities.TRANSPORT_CELLULAR);
        return isCellular;
    }

    //判断激活的网（默认网络）是否4G网
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isActivityWifi(Context context) {
        boolean isWifi = (getTransportType(context) == NetworkCapabilities.TRANSPORT_WIFI);
        return isWifi;
    }

    public static boolean isWifi(int transportType) {
        boolean isWifi = (transportType == NetworkCapabilities.TRANSPORT_WIFI);
        return isWifi;
    }

    //判断激活的网（默认网络）是否4G网
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isActivityEthernet(Context context) {
        boolean isEthernet = (getTransportType(context) == NetworkCapabilities.TRANSPORT_ETHERNET);
        return isEthernet;
    }

    public static boolean isEthernet(int transportType) {
        boolean isEthernet = (transportType == NetworkCapabilities.TRANSPORT_ETHERNET);
        return isEthernet;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static int getTransportType(Context context) {
        int transportType = -2;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            isConnected = false;
        } else {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                isConnected = true;
                transportType = getTransportType(networkCapabilities);
            } else {
                isConnected = false;
            }
        }
        return transportType;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void requestNetwork(Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        NetworkRequest networkRequest = builder.build();
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                if (Build.VERSION.SDK_INT >= 23) {
                    connectivityManager.bindProcessToNetwork(network);
                } else {
                    // 23后这个方法舍弃了
                    ConnectivityManager.setProcessDefaultNetwork(network);
                }
            }
        };
        final boolean[] chanelFlag = {true};
        executorService.scheduleWithFixedDelay(() -> {
            try {
                //网络都没连接直接返回
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (isActivityCellular(context)) {
                        return;
                    }
                }
                if (!isConnected) {
                    return;
                }
                isActivityConnected = ping();
                if (isActivityConnected) {
                    if (chanelFlag[0]) {
                        return;
                    }
                    chanelFlag[0] = true;
                    //1.以太网络可上外网时改为默认优先级上网
                    if (Build.VERSION.SDK_INT >= 23) {
                        connectivityManager.bindProcessToNetwork(null);
                    } else {
                        ConnectivityManager.setProcessDefaultNetwork(null);
                    }
                    connectivityManager.unregisterNetworkCallback(networkCallback);
                } else if (chanelFlag[0]) {
                    //2.以太网络不可上外网时自动切换蜂窝网
                    connectivityManager.requestNetwork(networkRequest, networkCallback);
                    chanelFlag[0] = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1, 5, TimeUnit.SECONDS);
    }

    // PING命令 使用新进程使用默认网络 不会使用 networkCallback 绑定的通道  用来判断以太网或者WiFi是否可上外网非常不错
    public static boolean ping() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process p = runtime.exec("ping -c 1 -W 1 www.baidu.com");
            int ret = p.waitFor();
            Log.i(TAG, "Process:" + ret);
            return ret == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}