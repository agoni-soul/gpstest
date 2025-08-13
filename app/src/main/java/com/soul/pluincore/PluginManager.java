package com.soul.pluincore;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.soul.gpstest.R;
import com.soul.main.plugin.system.DexClassLoader;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: haha
 * @date: 2025/8/2
 * Description: 插件管理器
 **/
public class PluginManager {
    private final static String TAG = PluginManager.class.getSimpleName();
    private static volatile PluginManager mInstance;
    private final Context mContext;
    private static volatile boolean sInitialized = false;

    private PluginManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public static PluginManager getInStance(Context context) {
        if (mInstance == null) {
            synchronized (PluginManager.class) {
                if (mInstance == null) {
                    mInstance = new PluginManager(context);
                }
            }
        }
        return mInstance;
    }

    public void init() {
        if (sInitialized) return;
        loadApk();
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(this::init);
            return;
        }
        try {
            // 1. 获取 ActivityThread
            Object activityThread = HookUtils.getHookActivityThread();
            if (activityThread == null) {
                Log.d(TAG, "init: activityThread = null");
                retryInit();
                return;
            }

            // 3. Hook
            hook();
        } catch (Exception e) {
            e.printStackTrace();
            retryInit();
        }
    }

    private void hook() {
        Log.d(TAG, "hook");
        try {
            HookUtils.hookAMS(mContext);
            HookUtils.hookHandler();
            // Hook Instrumentation
//            HookUtils.safeHookInstrumentation(this);
            HookUtils.hookInstrumentation(this);
            // 加载插件
            File pluginFile = new File(mContext.getExternalFilesDir(null).getAbsolutePath() + "/pluginapp-debug.apk");
            Log.d(TAG, "pluginFile = " + pluginFile.getAbsolutePath() + ", isExists = " + pluginFile.exists());

            if (pluginFile.exists()) {
                loadPlugin(pluginFile.getAbsolutePath());
            }
            PackageInfo packageInfo = mContext.getPackageManager()
                    .getPackageArchiveInfo(pluginFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                Log.d(TAG, "packageInfo = " + packageInfo.packageName);
            }
            sInitialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void retryInit() {
        new Handler(Looper.getMainLooper()).postDelayed(this::init, 300);
    }

    /**
     * 加载插件的dex文件，并且合并dexElements
     */
    private void loadApk() {
        try {
            // 加载插件的apk
            String apkPath = mContext.getFilesDir().getAbsolutePath() + "/";
            String pluginApkPath = mContext.getExternalFilesDir(null).getAbsolutePath() + "/pluginapp-debug.apk";
            String cachePath = mContext.getDir("cache_plugin", Context.MODE_PRIVATE).getAbsolutePath();
            DexClassLoader dexClassLoader = new DexClassLoader(pluginApkPath, cachePath, null, mContext.getClassLoader());

            Class<?> baseDexClassLoader = dexClassLoader.getClass().getSuperclass();
            Field pathListField = baseDexClassLoader.getDeclaredField("pathList");
            pathListField.setAccessible(true);

            // 获取plugin的的dexElements
            Object pluginPathListObject = pathListField.get(dexClassLoader);
            Class<?> pathListClass = pluginPathListObject.getClass();
            Field dexElementsField = pathListClass.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);
            Object pluginDexElements = dexElementsField.get(pluginPathListObject);

            // 获取host的dexElements, PathClassLoader
            ClassLoader pathClassLoader = mContext.getClassLoader();
            Field hostElementsField = pathListClass.getDeclaredField("dexElements");
            hostElementsField.setAccessible(true);
            Object hostPathListObject = pathListField.get(pathClassLoader);
            Object hostDexElements = hostElementsField.get(hostPathListObject);

            // 合并
            int pluginDexElementsLength = Array.getLength(pluginDexElements);
            int hostDexElementsLength = Array.getLength(hostDexElements);
            int newDexElementsLength = pluginDexElementsLength + hostDexElementsLength;

            Object newDexElements = Array.newInstance(hostDexElements.getClass().getComponentType(), newDexElementsLength);
            for (int i = 0; i < newDexElementsLength; i ++) {
                // plugin
                if (i < pluginDexElementsLength) {
                    Array.set(newDexElements, i, Array.get(pluginDexElements, i));
                } else {// host
                    Array.set(newDexElements, i, Array.get(hostDexElements, i - pluginDexElementsLength));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取插件的resource对象
     * @return 资源对象
     * @throws Exception
     */
    public Resources loadResources() throws Exception {
        try {
            String pluginApkPath = mContext.getExternalFilesDir(null).getAbsolutePath() + "/pluginapp-debug.apk";
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPathMethod = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            addAssetPathMethod.invoke(assetManager, pluginApkPath);

            Resources hostResources = mContext.getResources();
            Resources pluginResources = new Resources(
                    assetManager,
                    hostResources.getDisplayMetrics(),
                    hostResources.getConfiguration()
            );

            // 2. 创建合并后的Resources
            Resources mergedResources = new Resources(
                    assetManager,
                    hostResources.getDisplayMetrics(),
                    hostResources.getConfiguration()
            ) {
                @Override
                public int getIdentifier(String name, String defType, String defPackage) {
                    // 优先从插件获取主题资源
                    int id = pluginResources.getIdentifier(name, defType, defPackage);
                    return id != 0 ? id : super.getIdentifier(name, defType, defPackage);
                }
            };

//            // 3. 替换Activity的Resources
//            PluginApk pluginApk = mPlugins.get("pluginApkPath");
//            Context pluginContext = mContext.createPackageContext(pluginApk.getPackageName(),
//                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
//            pluginContext.getTheme().applyStyle(R.style.Theme_AppCompat, true);

            return mergedResources;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mContext.getResources();
    }

    private final Map<String, PluginApk> mPlugins = new ConcurrentHashMap<>();
    private final Map<String, String> mActivityPluginMap = new ConcurrentHashMap<>();

    public void loadPlugin(String apkPath) {
        try {
            PluginApk pluginApk = new PluginApk(mContext, apkPath);
            mPlugins.put(pluginApk.getPackageName(), pluginApk);

            // 注册插件中的 Activity
            for (String activity : pluginApk.getActivities()) {
                mActivityPluginMap.put(activity, pluginApk.getPackageName());
            }

            Log.i(TAG, "Loaded plugin: " + pluginApk.getPackageName());
        } catch (Exception e) {
            Log.e(TAG, "Failed to load plugin: " + apkPath, e);
        }
    }

    public boolean isPluginActivity(String className) {
        return mActivityPluginMap.containsKey(className);
    }

    public String getPluginNameForActivity(String className) {
        return mActivityPluginMap.get(className);
    }

    public PluginApk getPluginApk(String pluginName) {
        return mPlugins.get(pluginName);
    }

    public static class PluginApk {
        private final String mApkPath;
        private final String mPackageName;
        private final List<String> mActivities;
        private final DexClassLoader mClassLoader;
        private final Resources mResources;

        private Map<String, String> activityThemes = new HashMap<>(); // key: activity类名, value: 主题资源名称（例如："Theme.AppCompat.Light"）

        public void addActivityTheme(String activityClassName, String themeName) {
            activityThemes.put(activityClassName, themeName);
        }
        public String getActivityTheme(String activityClassName) {
            return activityThemes.get(activityClassName);
        }

        public PluginApk(Context context, String apkPath) throws Exception {
            mApkPath = apkPath;

            // 解析插件包信息
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);

            if (packageInfo == null || packageInfo.activities == null) {
                throw new RuntimeException("Invalid plugin APK");
            }

            mPackageName = packageInfo.packageName;

            // 收集 Activity 类名
            mActivities = new ArrayList<>();
            for (ActivityInfo activity : packageInfo.activities) {
                Log.d(TAG, "activity = " + activity.name);
                mActivities.add(activity.name);
            }

            // 创建插件 ClassLoader
            File optimizedDir = context.getDir("cache_plugin", Context.MODE_PRIVATE);
            mClassLoader = new DexClassLoader(
                    apkPath,
                    optimizedDir.getAbsolutePath(),
                    null,
                    context.getClassLoader());

            // 创建插件 Resources
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, apkPath);

            Resources hostResources = context.getResources();
            mResources = new Resources(
                    assetManager,
                    hostResources.getDisplayMetrics(),
                    hostResources.getConfiguration());
        }

        // Getter 方法
        public String getPackageName() { return mPackageName; }
        public List<String> getActivities() { return mActivities; }
        public ClassLoader getClassLoader() { return mClassLoader; }
        public Resources getResources() { return mResources; }
    }
}
