package com.soul.pluincore;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.soul.main.plugin.RegisteredActivity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author: haha
 * @date: 2025/8/3
 * Description:
 **/
public class AMSInvocationHandler implements InvocationHandler {
    private final String TAG = this.getClass().getSimpleName();

    private Context mContext;
    private Object mSubject;

    public AMSInvocationHandler(Context context, Object object) {
        mContext = context;
        mSubject = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("startActivity".equals(method.getName())) {
            Log.d(TAG, "AMSInvocationHandler startActivity invoke");
            // PluginActivity 替换成 RegisteredActivity
            for (int i = 0; i < args.length; i ++) {
                Object arg = args[i];
                if (arg instanceof Intent) {
                    Intent intentNew = new Intent();
                    intentNew.setClass(mContext, RegisteredActivity.class);
                    intentNew.putExtra("actionIntent", (Intent)arg);
                    args[i] = intentNew;
                    Log.d(TAG, "AMSInvocationHandler new Intent");
                    break;
                }
            }
        }
        return method.invoke(mSubject, args);
    }
}
