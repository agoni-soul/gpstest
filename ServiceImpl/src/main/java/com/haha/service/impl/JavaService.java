package com.haha.service.impl;

import android.util.Log;

import com.google.auto.service.AutoService;
import com.haha.service.api.Service;

@AutoService(Service.class)
public class JavaService implements Service {
  @Override
  public void start() {
    Log.d(Service.TAG, "Loading java service");
    System.out.println("Loading java service");
  }

  @Override
  public String getUserName() {
    return "JavaService";
  }
}
