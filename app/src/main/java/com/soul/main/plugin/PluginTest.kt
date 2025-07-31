package com.soul.main.plugin

import android.util.Log

/**
 *
 * @author:     haha
 * @date:       2025/7/31
 * Description: 插件化学习
 *
 **/
public class PluginTest {
    private val TAG = this.javaClass.simpleName

    public fun test() {
        Log.d(TAG, "String.classLoader = ${String::class.java.classLoader}")
        Log.d(TAG, "String.classLoader = ${String::class.java.classLoader}")
    }
}