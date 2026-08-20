package com.haha.main.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * @auther: haha
 * @Date: 2026/1/5
 * @Detail:
 */
public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("MyReceiver接受到消息");
        // 获取传递的数据
        String data = getResultData();
        System.out.println("data = " + data);
        Bundle bundle = getResultExtras(true);

        // 修改广播数据（只有有序广播可以）
        setResultData("Modified data");
        bundle.putString("extra", "new value");
        setResultExtras(bundle);

        // 中止广播（阻止传递给低优先级的接收器）
        abortBroadcast();

        // 获取优先级
        int priority = getResultCode();
        System.out.println("priority = " + priority);
    }
}