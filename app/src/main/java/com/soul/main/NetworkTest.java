package com.soul.main;
//
//import android.os.UserHandle;
//import android.util.Log;
//
//import java.lang.reflect.Method;
//
///**
// * <pre>
// *     author : yangzy33
// *     e-mail : yangzy33@midea.com
// *     time   : 2022/10/18
// *     desc   :
// *     version: 1.0
// * </pre>
// */
public class NetworkTest {
//    private final String TAG = this.getClass().getName();
//    /**
//     * 设置底层后台流量数据控制接口（通讯组接口）
//     * @param uid 应用的UID
//     * @param isWifiControl WIFI控制：true为打开，false为关闭
//     * @param isMobileControl Mobile控制（与wifi类似）
//     */
//    public void appBackGourndNetControl(int uid, boolean isWifiControl, boolean isMobileControl) {
//        Log.i(TAG, "appBackGourndNetControl: uid:" + uid + " wifi:" + isWifiControl + " mobile:" + isMobileControl);
//        if (isWifiControl) {
//            //打开wifi
//            if (!networkControllManager.isAppWifiBackgroundUsageOpened(uid)) {
//                Log.d(TAG, "appBackGourndNetControl: change wifibackground open:" + uid);
//                networkControllManager.openAppWifiBackgroundUsage(uid);
//            }
//
//        } else {
//            //关闭wifi
//            if (networkControllManager.isAppWifiBackgroundUsageOpened(uid)) {
//                Log.d(TAG, "appBackGourndNetControl: change wifibackground close:" + uid);
//                networkControllManager.closeAppWifiBackgroundUsage(uid);
//            }
//        }
//
//        if (isMobileControl) {
//            //打开后台移动网络
//            if (!networkControllManager.isAppMobileBackgroundUsageOpened(uid)) {
//                Log.d(TAG, "appBackGourndNetControl: change mobilebackground open:" + uid);
//                networkControllManager.openAppMobileBackgroundUsage(uid);
//            }
//        } else {
//            //关闭后台移动网络
//            if (networkControllManager.isAppMobileBackgroundUsageOpened(uid)) {
//                Log.d(TAG, "appBackGourndNetControl: change mobilebackground close:" + uid);
//                networkControllManager.closeAppMobileBackgroundUsage(uid);
//            }
//        }
//    }
//
//    public void closeAppWifiBackgroundUsage(String pkName) {
//        int uid = PackageManagerUtil.getUid(pkName);
//        Log.i(TAG, "close uid=" + uid + " background wifi net");
//        addUidPolicy(new Object[]{uid, getPolicyRejectAppBackgroundNetWifi()});
//    }
//
//    /**
//     * 关闭网络．
//     * @param args
//     */
//    private void addUidPolicy(Object... args) {
//        Method addUidPolicyMethod = getAddUidPolicyMethod();
//        try {
//            addUidPolicyMethod.invoke(mNetWorkPolicyManager, args);
//        } catch (IllegalAccessException e) {
//            Log.e(TrafficConst.TRAFFIC_EXCEPTION, TAG + ":addUidPolicy --> " + e.toString());
//        } catch (IllegalArgumentException e) {
//            Log.e(TrafficConst.TRAFFIC_EXCEPTION, TAG + ":addUidPolicy --> " + e.toString());
//        } catch (Exception e) {
//            Log.e(TrafficConst.TRAFFIC_EXCEPTION, TAG + ":addUidPolicy --> " + e.toString());
//        }
//    }
//
//    @Override
//    public void addUidPolicy(int uid, int policy) {
//        mContext.enforceCallingOrSelfPermission(MANAGE_NETWORK_POLICY, TAG);
//
//        if (!UserHandle.isApp(uid)) {
//            throw new IllegalArgumentException("cannot apply policy to UID " + uid);
//        }
//
//        synchronized (mRulesLock) {
//            final int oldPolicy = mUidPolicy.get(uid, POLICY_NONE);
//            policy |= oldPolicy;
//            if (oldPolicy != policy) {
//                setUidPolicyUncheckedLocked(uid, oldPolicy, policy, true);
//            }
//        }
//    }
//
//    private void setUidPolicyUncheckedLocked(int uid, int policy, boolean persist) {
//
//
//        mLastUidPolicy = getUidPolicy(uid);
//
//
//        mUidPolicy.put(uid, policy);
//
//
//
//        Slog.d(TAG, "mLastUidPolicy " + mLastUidPolicy + " " + policy);
//        updateRulesForUidWifiAndMobileLocked(uid);
//
//
//        // uid policy changed, recompute rules and persist policy.
//        updateRulesForDataUsageRestrictionsLocked(uid);
//        if (persist) {
//            writePolicyLocked();
//        }
//    }
//
//
}
