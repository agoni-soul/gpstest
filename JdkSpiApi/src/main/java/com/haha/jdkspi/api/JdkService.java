package com.haha.jdkspi.api;

/**
 * JDK {@code java.util.ServiceLoader} 示例接口，与组件化 {@code IUserService} 无关。
 */
public interface JdkService {
    String TAG = "JdkService";

    void start();

    String getUserName();
}
