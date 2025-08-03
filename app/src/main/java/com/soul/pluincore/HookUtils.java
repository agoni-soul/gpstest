package com.soul.pluincore;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author: haha
 * @date: 2025/8/3
 * Description: hook工具类
 **/
public class HookUtils {

    /**
     * hook  AMS对象
     * 对AMS中startActivity方法进行拦截
     * @param context
     */
    public static void hookAMS(Context context) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        // 1.获取到AMS对象
        // 1.1 获取静态属性 ActivityManager.IActivityManagerSingleton的值
        // 它是Singleton类型
        Field iActivityManagerSingletonField = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Class<?> activityManagerNativeClazz = Class.forName("android.app.ActivityManagerNative");
            iActivityManagerSingletonField = activityManagerNativeClazz.getDeclaredField("gDefault");
        } else {
            iActivityManagerSingletonField = ActivityManager.class.getDeclaredField("IActivityManagerSingleton");
        }
        iActivityManagerSingletonField.setAccessible(true);
        Object iActivityManagerSingletonObject = iActivityManagerSingletonField.get(null);

        // 1.2 获取Singleton的mInstance值
        Class<?> singletonClazz = Class.forName("android.util.Singleton");
        Field mInstanceField = singletonClazz.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);
        Object AMSSubject = mInstanceField.get(iActivityManagerSingletonObject);

        // 2.对AMS对象进行代理
        // TODO 代理对象相关知识？？
        Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
        AMSInvocationHandler handler = new AMSInvocationHandler(context, AMSSubject);
        Object AMSProxy = Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] {iActivityManagerInterface},
                handler
        );
        mInstanceField.set(iActivityManagerSingletonObject, AMSProxy);

        // 3.InvocationHandler 对AMS对象的方法进行拦截
    }

    /**
     * 获取到Handler特定消息中的Intent，进行处理
     * 将Intent对象里的RegisteredActivity替换成PluginActivity
     */
    public static void hookHandler() throws Exception {
        // 1.获取到Handler对象(mH属性值)
        // 1.1 获取ActivityThread对象
        Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
        Field sCurrentActivityThreadField = activityThreadClazz.getDeclaredField("sCurrentActivityThread");
        sCurrentActivityThreadField.setAccessible(true);
        Object activityThreadObject = sCurrentActivityThreadField.get(null);

        // 1.2 获取ActivityThread对象的吗H的属性值
        Field mHField = activityThreadClazz.getDeclaredField("mH");
        mHField.setAccessible(true);
        Object handler = mHField.get(activityThreadObject);

        // 2.给Handler的mCallback属性值赋值
        Field mCallbackField = Handler.class.getDeclaredField("mCallback");
        mCallbackField.setAccessible(true);
        mCallbackField.set(handler, new PluginCallback());

        // 3.在Callback中奖Intent对象里的RegisteredActivity替换成PluginActivity
    }
}
