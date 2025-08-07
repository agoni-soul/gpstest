package com.soul.pluincore;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author: haha
 * @date: 2025/8/3
 * Description: hook工具类
 **/
public class HookUtils {

    private final static String TAG = HookUtils.class.getSimpleName();
    private volatile static Class<?> mActivityThreadClazz;


    /**
     * hook  AMS对象
     * 对AMS中startActivity方法进行拦截
     *
     * @param context
     */
    public static void hookAMS(Context context) {
        Log.d(TAG, "hookAMS start");
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {// 1.获取到AMS对象
                // 1.1 获取静态属性 ActivityManager.IActivityManagerSingleton的值
                // 它是Singleton类型
                Field iActivityManagerSingletonField;
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
                Log.d(TAG, "hookAMS: AMSSubject = " + AMSSubject);
                // 2.对AMS对象进行代理
                // TODO 代理对象相关知识？？
                Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
                AMSInvocationHandler handler = new AMSInvocationHandler(context, AMSSubject);
                Object AMSProxy = Proxy.newProxyInstance(
                        Thread.currentThread().getContextClassLoader(),
                        new Class[]{iActivityManagerInterface},
                        handler
                );
                Log.d(TAG, "hookAMS: AMSProxy = " + AMSProxy);
                mInstanceField.set(iActivityManagerSingletonObject, AMSProxy);
                Log.d(TAG, "hookAMS: mInstanceField.value = " + mInstanceField.get(iActivityManagerSingletonObject));
            } else {
                Class<?> activityTaskManagerClazz = Class.forName("android.app.ActivityTaskManager");
                Field iATMSingletonField = activityTaskManagerClazz.getDeclaredField("IActivityTaskManagerSingleton");
                iATMSingletonField.setAccessible(true);
                Log.d(TAG, "hookATM: iATMSingletonField = " + iATMSingletonField);
                Object iATMObject = iATMSingletonField.get(null);
                Log.d(TAG, "hookATM: iATMObject = " + iATMObject);

                // 1.2 获取Singleton的mInstance值
                Class<?> singletonATMClazz = Class.forName("android.util.Singleton");
                Field mInstanceATMField = singletonATMClazz.getDeclaredField("mInstance");
                mInstanceATMField.setAccessible(true);
                Log.d(TAG, "hookATM: mInstanceField = " + mInstanceATMField);
                Object ATMObject = mInstanceATMField.get(iATMObject);
                Log.d(TAG, "hookATM: ATMObject = " + ATMObject);

                Class<?> iATMInterface = Class.forName("android.app.IActivityTaskManager");
                ATMSInvocationHandler ATMSHandler = new ATMSInvocationHandler(context, ATMObject);
                Object ATMSProxy = Proxy.newProxyInstance(
                        Thread.currentThread().getContextClassLoader(),
                        new Class[]{iATMInterface},
                        ATMSHandler
                );
                Log.d(TAG, "hookATM: ATMSProxy = " + ATMSProxy);
                mInstanceATMField.set(iATMObject, ATMSProxy);
                Log.d(TAG, "hookATM: mInstanceATMField.value = " + mInstanceATMField.get(iATMObject));
            }

            // 3.InvocationHandler 对AMS对象的方法进行拦截
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取到Handler特定消息中的Intent，进行处理
     * 将Intent对象里的RegisteredActivity替换成PluginActivity
     */
    public static void hookHandler() {
        Log.d(TAG, "hookHandler start");
        try {
            Object activityThreadObject = getHookActivityThread();

            // 1.2 获取ActivityThread对象的吗H的属性值
            Field mHField = getActivityThreadClazz().getDeclaredField("mH");
            mHField.setAccessible(true);
            Object handler = mHField.get(activityThreadObject);

            // 2.给Handler的mCallback属性值赋值
            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            mCallbackField.set(handler, new PluginCallback());

            // 3.在Callback中奖Intent对象里的RegisteredActivity替换成PluginActivity
        } catch (Exception e) {// 回退方案
        }
    }

    public static Class<?> getActivityThreadClazz() {
        try {
            if (mActivityThreadClazz == null) {
                synchronized (HookUtils.class) {
                    if (mActivityThreadClazz == null) {
                        mActivityThreadClazz = Class.forName("android.app.ActivityThread");
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return mActivityThreadClazz;
    }

    public static Object getHookActivityThread() {
        Object object = null;
        try {
            // 1.获取到Handler对象(mH属性值)
            // 1.1 获取ActivityThread对象
            Field sCurrentActivityThreadField = getActivityThreadClazz().getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            object = sCurrentActivityThreadField.get(null);
        } catch (Exception e) {
            try {
                Class<?> appGlobalsClass = Class.forName("android.app.AppGlobals");
                Method getActivityThreadMethod = appGlobalsClass.getMethod("getActivityThread");
                object = getActivityThreadMethod.invoke(null);
            } catch (Exception ex) {
                e.printStackTrace();
            }
        }
        return object;
    }

    public static void hookInstrumentation(PluginManager pluginManager) {
        Log.d(TAG, "hookInstrumentation start");
        try {

            Object sCurrentActivityThread = getHookActivityThread();
            Field instrumentationField = getActivityThreadClazz().getDeclaredField("mInstrumentation");
            instrumentationField.setAccessible(true);
            Instrumentation instrumentation = (Instrumentation) instrumentationField.get(sCurrentActivityThread);
            Log.d(TAG, "hookInstrumentation: instrumentation = " + instrumentation);

            // 3. 创建并替换为自定义 Instrumentation
            if (!(instrumentation instanceof PluginInstrumentation)) {
                instrumentationField.set(sCurrentActivityThread, new PluginInstrumentation(instrumentation, pluginManager));
            }
            Log.d(TAG, "hookInstrumentation: instrumentation new = " + instrumentation);
            for (Field field : instrumentation.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Log.e(TAG, "Instrumentation: Field = " + field);
            }
//            Field threadInInstrumentationField = Instrumentation.class.getField("mThread");
//            threadInInstrumentationField.setAccessible(true);
//            Object threadInInstrumentation = threadInInstrumentationField.get(instrumentation);
//            if (threadInInstrumentation == null) {
//                threadInInstrumentationField.set(instrumentation, sCurrentActivityThread);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hookInstrumentation(PluginManager pluginManager, Activity activity) {
        try {
            Class<?> activityClazz = activity.getClass();
            Field instrumentationField = activityClazz.getDeclaredField("mInstrumentation");
            instrumentationField.setAccessible(true);
            Instrumentation instrumentation = (Instrumentation) instrumentationField.get(activity);
            if (!(instrumentation instanceof PluginInstrumentation)) {
                instrumentationField.set(activity, new ProxyInstrumentation(instrumentation, pluginManager));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void safeHookInstrumentation(PluginManager pluginManager) {
        try {
            Object activityThread = getHookActivityThread();
            if (activityThread == null) {
                // 延迟重试
                new Handler(Looper.getMainLooper()).postDelayed(() -> safeHookInstrumentation(pluginManager), 500);
                return;
            }

            Field instrumentationField = activityThread.getClass().getDeclaredField("mInstrumentation");
            instrumentationField.setAccessible(true);
            Instrumentation instrumentation = (Instrumentation) instrumentationField.get(activityThread);
            Log.d(TAG, "safeHookInstrumentation: instrumentation = " + instrumentation);

            if (!(instrumentation instanceof PluginInstrumentation)) {
                instrumentationField.set(activityThread, new PluginInstrumentation(instrumentation, pluginManager));
            }
            Log.d(TAG, "hookInstrumentation: instrumentation new = " + instrumentation);

            Field threadInInstrumentationField = instrumentationField.getClass().getField("mThread");
            threadInInstrumentationField.setAccessible(true);
            Object threadInInstrumentation = threadInInstrumentationField.get(instrumentation);
            Log.d(TAG, "safeHookInstrumentation: threadInInstrumentation = " + threadInInstrumentation);
            if (threadInInstrumentation == null) {
                threadInInstrumentationField.set(instrumentation, activityThread);
            }
            Log.d(TAG, "safeHookInstrumentation: threadInInstrumentation new = " + threadInInstrumentation);
        } catch (Exception e) {
            Log.e("Hook", "Failed to hook instrumentation", e);
        }
    }
}
