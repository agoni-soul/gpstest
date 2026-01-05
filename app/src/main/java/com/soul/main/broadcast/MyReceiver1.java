package com.soul.main.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @auther: haha
 * @Date: 2026/1/5
 * @Detail:
 */
public class MyReceiver1 extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("MyReceiver1接受到消息");
    }
}