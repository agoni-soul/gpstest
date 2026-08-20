package com.haha.leakcanary

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Debug
import android.util.Log
import leakcanary.AppWatcher
import leakcanary.EventListener
import leakcanary.LeakCanary

/**
 * Debug 包显式安装 LeakCanary，避免只改 config、AppWatcher 实际未安装。
 *
 * LeakCanary 2.x 不再提供 1.x 的 [LeakCanary.install]；默认靠 App Startup 自动安装。
 * Flutter / 多 Provider 合并时自动安装可能失败，因此关闭自动安装并在此 manualInstall。
 */
object LeakCanaryInstaller {
    private const val TAG = "LeakCanaryInstaller"

    fun install(application: Application) {
        if (!AppWatcher.isInstalled) {
            AppWatcher.manualInstall(application)
            Log.i(TAG, "AppWatcher.manualInstall() 完成")
        } else {
            Log.i(TAG, "AppWatcher 已自动安装，跳过 manualInstall")
        }

        AppWatcher.config = AppWatcher.config.copy(
            watchActivities = true,
            watchFragments = true,
            watchViewModels = true,
            watchFragmentViews = false,
        )

        val junitOnClasspath = try {
            Class.forName("org.junit.Test")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
        if (junitOnClasspath) {
            Log.w(
                TAG,
                "检测到 org.junit.Test 在运行时 classpath。LeakCanary 默认会因此关闭 dump，" +
                        "已用 leak_canary_test_class_name 覆盖；同时请把 junit 改成 test/androidTestImplementation"
            )
        }

        LeakCanary.config = LeakCanary.config.copy(
            dumpHeap = true,
            dumpHeapWhenDebugging = true,
            retainedVisibleThreshold = 1,
            eventListeners = LeakCanary.config.eventListeners + EventListener { event ->
                Log.i(TAG, "analysis event=${event.javaClass.simpleName}")
            },
        )
        LeakCanary.showLeakDisplayActivityLauncherIcon(true)

        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

            override fun onActivityDestroyed(activity: Activity) {
                val watcher = AppWatcher.objectWatcher
                Log.i(
                    TAG,
                    "onDestroy ${activity.javaClass.name}, " +
                            "hasWatched=${watcher.hasWatchedObjects}, " +
                            "retained=${watcher.retainedObjectCount}"
                )
            }
        })

        Log.i(
            TAG,
            "installed=${AppWatcher.isInstalled}, " +
                    "watchActivities=${AppWatcher.config.watchActivities}, " +
                    "dumpHeap=${LeakCanary.config.dumpHeap}, " +
                    "dumpHeapWhenDebugging=${LeakCanary.config.dumpHeapWhenDebugging}, " +
                    "retainedVisibleThreshold=${LeakCanary.config.retainedVisibleThreshold}, " +
                    "debuggerAttached=${Debug.isDebuggerConnected()}, " +
                    "junitOnClasspath=$junitOnClasspath"
        )
    }
}
