package com.haha.serviceImpl;

import android.util.Log;

import com.google.auto.service.AutoService;
import com.haha.serviceApi.Service;

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
