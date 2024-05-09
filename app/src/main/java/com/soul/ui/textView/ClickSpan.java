package com.soul.ui.textView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import java.io.File;

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/07/01
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class ClickSpan extends ClickableSpan {

    private String mUrl;

    private String mRouterSSID;

    private int mCode;

    public final static int CODE_SERVICE = 0;

    public final static int CODE_WIFITEST = 1;

    public final static int CODE_WEEX = 2;

    public final static int CODE_ARTICLE = 3;

    private static final int WIFI_STATE_NORMAL = 0;

    private static final int WIFI_STATE_DISABLE = 1;

    private static final int WIFI_STATE_NOT_SAME = 2;

    private int mWifiState = WIFI_STATE_NORMAL;

    public ClickSpan(int code, String url) {
        mCode = code;
        mUrl = url;
    }

    @Override
    public void onClick(@NonNull View view) {
        if (mCode == CODE_SERVICE){
            Log.d("haha", "CODE_SERVICE");
        }else if(mCode == CODE_WIFITEST){
            Log.d("haha", "CODE_WIFITEST");
        }else if(mCode == CODE_WEEX){
            Log.d("haha", "CODE_WEEX");
        }else if (mCode == CODE_ARTICLE){
            Log.d("haha", "CODE_ARTICLE");
        }
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(Color.parseColor("#267AFF"));
        ds.setUnderlineText(false);  //去掉下划线
    }
}
