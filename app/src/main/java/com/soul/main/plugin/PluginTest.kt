package com.soul.main.plugin

import android.app.Activity
import android.content.Context
import android.os.Environment
import android.util.Log
import com.soul.main.MainActivity
import com.soul.main.plugin.system.DexClassLoader
import java.io.File

/**
 *
 * @author:     haha
 * @date:       2025/7/31
 * Description: 插件化学习
 *
 **/
object PluginTest {
    private val TAG = javaClass.simpleName

    fun test(context: Context) {
        Log.d(TAG, "String.classLoader = ${String::class.java.classLoader}")
        Log.d(TAG, "Activity.classLoader = ${Activity::class.java.classLoader}")
        Log.d(TAG, "MainActivity.classLoader = ${MainActivity::class.java.classLoader}")
        Log.d(TAG, "PluginTest.classLoader = ${PluginTest::class.java.classLoader}")

        val file = File(context.filesDir, "classes.dex")
        Log.d(TAG, "file = ${file.absolutePath}, exists = ${file.exists()}")
        Log.d(TAG, "文件可读: ${file.canRead()}")
        Log.d(TAG, "文件可写: ${file.canWrite()}")

        // dex -> odex
        val dir = context.getDir("cache_plugin", Context.MODE_PRIVATE)
        Log.d(TAG, "dir = ${dir.absolutePath}, exists = ${dir.exists()}")
        // parent ? PathClassLoader 不是父类，是兄弟关系
        val dexClassLoader = DexClassLoader(file.absolutePath, dir.absolutePath, null, context.classLoader)

        try {
            val clazz = dexClassLoader.loadClass("com.soul.main.plugin.MyPlugin")
            clazz.getMethod("doSomething").invoke(clazz.newInstance())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun init(context: Context) {
        try {
            val clazz = Class.forName("com.soul.pluginapp.PluginActvity")
            clazz.getMethod("doSomething").invoke(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}