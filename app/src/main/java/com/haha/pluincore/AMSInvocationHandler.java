package com.haha.pluincore;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.haha.main.plugin.RegisteredActivity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author: haha
 * @date: 2025/8/3
 * Description:
 **/
public class AMSInvocationHandler implements InvocationHandler {
    private final String TAG = this.getClass().getSimpleName();

    private final Context mContext;
    private final Object mSubject;

    public AMSInvocationHandler(Context context, Object object) {
        mContext = context;
        mSubject = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.d(TAG, TAG + " invoke: Method = " + method.getName() + ", proxy = " + proxy.getClass());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && "startActivity".equals(method.getName())) {
            Log.d(TAG, TAG + " startActivity invoke");
            // PluginActivity 替换成 RegisteredActivity
            for (int i = 0; i < args.length; i ++) {
                Object arg = args[i];
                if (arg instanceof Intent) {
                    Intent intentNew = new Intent();
                    intentNew.setClass(mContext, RegisteredActivity.class);
                    intentNew.putExtra("actionIntent", (Intent)arg);
                    args[i] = intentNew;
                    Log.d(TAG, TAG + " new Intent");
                    break;
                }
            }
        }
        return method.invoke(mSubject, args);
    }
}
