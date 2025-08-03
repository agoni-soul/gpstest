package com.soul.main.plugin;

/**
 * @author: haha
 * @date: 2025/7/31
 * Description: 插件化测试类
 **/
public class MyPlugin {
    SubPlugin subPlugin = new SubPlugin();

    public void doSomething() {
        subPlugin.doSomething();
    }
}
