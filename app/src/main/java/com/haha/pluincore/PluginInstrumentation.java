package com.haha.pluincore;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.haha.gpstest.R;
import com.haha.main.plugin.RegisteredActivity;

import java.lang.reflect.Method;

/**
 * @auther: haha
 * @Date: 2025/8/4
 * @Detail: hook Instrumentation
 */
public class PluginInstrumentation extends Instrumentation {

    private static final String TAG = PluginInstrumentation.class.getSimpleName();
    private final Instrumentation mBase;
    private final PluginManager mPluginManager;

    public PluginInstrumentation(Instrumentation base, PluginManager pluginManager) {
        mBase = base;
        mPluginManager = pluginManager;
    }

    /**
     * Called when the instrumentation is starting, before any application code
     * has been loaded.  Usually this will be implemented to simply call
     * {@link #start} to begin the instrumentation thread, which will then
     * continue execution in {@link #onStart}.
     *
     * <p>If you do not need your own thread -- that is you are writing your
     * instrumentation to be completely asynchronous (returning to the event
     * loop so that the application can run), you can simply begin your
     * instrumentation here, for example call {@link Context#startActivity} to
     * begin the appropriate first activity of the application.
     *
     * @param arguments Any additional arguments that were supplied when the
     *                  instrumentation was started.
     */
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mBase.onCreate(arguments);
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        // 检查是否是插件 Activity
        ComponentName componentName = intent.getComponent();
        Log.d(TAG, "newActivity: componentName = " + componentName);
        String pkg = intent.getComponent() != null ? intent.getComponent().getPackageName() : null;

        if (componentName != null && mPluginManager.isPluginActivity(componentName.getClassName())) {
            String pluginName = mPluginManager.getPluginNameForActivity(componentName.getClassName());
            Log.d(TAG, "newActivity: pluginName = " + pluginName);
            PluginManager.PluginApk pluginApk = mPluginManager.getPluginApk(pluginName);

            if (pluginApk != null) {
                // 使用插件的 ClassLoader
                ClassLoader pluginClassLoader = pluginApk.getClassLoader();
                Log.d(TAG, "newActivity: pluginClassLoader = " + pluginClassLoader);
                // 强制设置AppCompat主题
                Activity activity =mBase.newActivity(pluginClassLoader, className, intent);
                activity.setTheme(R.style.Theme_AppCompat_Light);
                return activity;
            }
        }
        return super.newActivity(cl, className, intent);
    }

//    @Override
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        // 拦截启动请求
        ComponentName component = intent.getComponent();
        if (component != null && mPluginManager.isPluginActivity(component.getClassName())) {
            // 替换为占位 Activity
            intent.putExtra("actionIntent", intent.cloneFilter());
            intent.setClassName(who, RegisteredActivity.class.getName());
        }

        try {
            // 反射调用原始方法
            Method execStartActivity = Instrumentation.class.getDeclaredMethod(
                    "execStartActivity",
                    Context.class, IBinder.class, IBinder.class, Activity.class,
                    Intent.class, int.class, Bundle.class);
            execStartActivity.setAccessible(true);

            return (ActivityResult) execStartActivity.invoke(
                    mBase, who, contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
