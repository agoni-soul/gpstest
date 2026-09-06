package com.haha.jdkspi.impl;

import android.util.Log;

import com.google.auto.service.AutoService;
import com.haha.jdkspi.api.JdkService;

@AutoService(JdkService.class)
public class JavaService implements JdkService {
    @Override
    public void start() {
        Log.d(JdkService.TAG, "Loading java service");
        System.out.println("Loading java service");
    }

    @Override
    public String getUserName() {
        return "JavaService";
    }
}
