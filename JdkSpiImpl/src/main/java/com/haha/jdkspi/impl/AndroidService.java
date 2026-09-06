package com.haha.jdkspi.impl;

import android.util.Log;

import com.google.auto.service.AutoService;
import com.haha.jdkspi.api.JdkService;

@AutoService(JdkService.class)
public class AndroidService implements JdkService {
    @Override
    public void start() {
        Log.d(JdkService.TAG, "Loading android service");
        System.out.println("Loading android service");
    }

    @Override
    public String getUserName() {
        return "AndroidService";
    }
}
