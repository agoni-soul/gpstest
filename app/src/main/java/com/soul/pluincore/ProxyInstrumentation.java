package com.soul.pluincore;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.soul.main.plugin.RegisteredActivity;

import java.lang.reflect.Method;

/**
 * @auther: soulagoni
 * @Date: 2025/8/7
 * @Detail:
 */
public class ProxyInstrumentation extends Instrumentation {
    private static final String TAG = ProxyInstrumentation.class.getSimpleName();
    Instrumentation mBase;
    PluginManager mPluginManager;

    public ProxyInstrumentation(Instrumentation base, PluginManager pluginManager) {
        mBase = base;
        mPluginManager = pluginManager;
    }

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
            Method execStartActivityMethod = Instrumentation.class.getDeclaredMethod(
                    "execStartActivity",
                    Context.class, IBinder.class, IBinder.class, Activity.class,
                    Intent.class, int.class, Bundle.class);
            execStartActivityMethod.setAccessible(true);
            return (ActivityResult) execStartActivityMethod.invoke(mBase, who, contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
