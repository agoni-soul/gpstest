package com.soul.pluincore;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.soul.main.plugin.system.DexClassLoader;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author: haha
 * @date: 2025/8/2
 * Description: 插件管理器
 **/
public class PluginManager {
    private final String TAG = this.getClass().getSimpleName();
    private static volatile PluginManager mInstance;
    private final Context mContext;

    private PluginManager(Context context) {
        mContext = context;
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
        try {
            loadApk();
            HookUtils.hookAMS(mContext);
            HookUtils.hookHandler();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载插件的dex文件，并且合并dexElements
     */
    private void loadApk() throws Exception {
        // 加载插件的apk
        String apkPath = mContext.getFilesDir().getAbsolutePath() + "/";
        Log.d(TAG, "apkPath = " + apkPath);
        String pluginApkPath = mContext.getExternalFilesDir(null).getAbsolutePath() + "/pluginapp-debug.apk";
        Log.d(TAG, "pluginApkPath = " + pluginApkPath);
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
        Log.d(TAG, "pluginDexElements = " + pluginDexElements);

        // 获取host的dexElements, PathClassLoader
        ClassLoader pathClassLoader = mContext.getClassLoader();
        Object hostPathListObject = pathListField.get(pathClassLoader);
        Object hostDexElements = dexElementsField.get(hostPathListObject);

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
    }

    /**
     * 获取插件的resource对象
     * @return 资源对象
     * @throws Exception
     */
    public Resources loadResources() throws Exception {
        String pluginApkPath = mContext.getExternalFilesDir(null).getAbsolutePath() + "/pluginapp-debug.apk";
        AssetManager assetManager = AssetManager.class.newInstance();
        Method addAssetPathMethod = AssetManager.class.getMethod("addAssetPath", String.class);
        addAssetPathMethod.setAccessible(true);
        addAssetPathMethod.invoke(assetManager, pluginApkPath);
        return new Resources(assetManager, mContext.getResources().getDisplayMetrics(), mContext.getResources().getConfiguration());
    }
}
