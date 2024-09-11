package com.haha.service.impl;

import android.util.Log;

import com.google.auto.service.AutoService;
import com.haha.service.api.Service;

@AutoService(Service.class)
public class AndroidService implements Service {
  @Override
  public void start() {
    Log.d(Service.TAG, "Loading android service");
    System.out.println("Loading android service");
  }

  @Override
  public String getUserName() {
    return "AndroidService";
  }
}
