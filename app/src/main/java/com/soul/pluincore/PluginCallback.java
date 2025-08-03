package com.soul.pluincore;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author: haha
 * @date: 2025/8/3
 * Description: 自定义Handler中的Callback
 **/
public class PluginCallback implements Handler.Callback {
    private String TAG = this.getClass().getSimpleName();

    private static final int LAUNCH_ACTIVITY = 100;
    public static final int EXECUTE_TRANSACTION = 159;

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case LAUNCH_ACTIVITY: {
                Log.d(TAG, "PluginCallback handleMessage LAUNCH_ACTIVITY");
                try {
                    Field intentField = msg.obj.getClass().getDeclaredField("intent");
                    intentField.setAccessible(true);
                    Intent intent = (Intent) intentField.get(msg.obj);
                    Parcelable actionIntent = intent.getParcelableExtra("actionIntent");
                    if (actionIntent != null) {
                        Log.d(TAG, "PluginCallback handleMessage intent replaced");
                        intentField.set(msg.obj, actionIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case EXECUTE_TRANSACTION: {
                Log.d(TAG, "PluginCallback handleMessage EXECUTE_TRANSACTION");
                try {
                    // Intent ?
                    // 1. 获取mActivityCallbacks集合
                    Object clientTransactionObject = msg.obj;
                    Class<?> clientTransactionClazz = clientTransactionObject.getClass();
                    Field mActivityCallbacksField = clientTransactionClazz.getDeclaredField("mActivityCallbacks");
                    mActivityCallbacksField.setAccessible(true);

                    List mActivityCallbacks = (List) mActivityCallbacksField.get(clientTransactionObject);
                    Class<?> launchActivityItemClazz = Class.forName("android.app.servertransaction.LaunchActivityItem");

                    // 2. 遍历mActivityCallbacks，得到LaunchActivityItem
                    for (Object item : mActivityCallbacks) {
                        if ("android.app.servertransaction.LaunchActivityItem".equals(item.getClass().getName())) {
                            // 3. 替换LaunchActivityItem的Intent
                            Field intentField = item.getClass().getDeclaredField("mIntent");
                            intentField.setAccessible(true);
                            Intent intent = (Intent) intentField.get(item);
                            Parcelable actionIntent = intent.getParcelableExtra("actionIntent");
                            if (actionIntent != null) {
                                Log.d(TAG, "PluginCallback handleMessage intent replaced");
                                intentField.set(item, actionIntent);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return false;
    }
}
