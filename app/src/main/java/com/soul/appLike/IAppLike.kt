package com.soul.appLike

import android.content.Context


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
interface IAppLike {
    companion object {
        // Application主线程立刻初始化
        val MAX_PRIORITY = 10
        val NORM_PRIORITY = 5
        val MIN_PRIORITY = 1

        // Application主线程延迟初始化
        val PRIORITY_MAIN_DELAY_MAX = 10 - 11
        val PRIORITY_MAIN_DELAY = 5 - 11
        val PRIORITY_MAIN_DELAY_MIN = 1 - 11

        // Application子线程延迟初始化
        val PRIORITY_SUB_DEALY_MAX = 10 - 21
        val PRIORITY_SUB_DEALY = 5 - 21
        val PRIORITY_SUB_DEALY_MIN = 1 - 21

        // 每个time type对应不同的初始化时机
        val TIME_TYPE_MAIN = 1 // 主线程立刻初始化

        val TIME_TYPE_MAIN_DELAY = 2 // 主线程延迟初始化

        val TIME_TYPE_SUB_DELAY = 3 // 子线程延迟初始化

        val TIME_TYPE_THIRD_SDK = 4 // 第三方SDK初始化

        val TIME_TYPE_NEED_AGREE_PRIVACY_SUB = 5 // 同意隐私协议之后运行在子线程

        val TIME_TYPE_SYSTEM_IDLE_HANDLE = 6 // 系统闲时初始化
    }

    // 初始化优先级
    @Deprecated("")
    fun getPriority(): Int

    @Deprecated("")
    fun onCreate(context: Context?)


    // onCreate，根据各time type的时机来做初始化
    // 注意，不同时机，不同的timeType都会回调该函数，准确说是4次，所以需要工程师根据选择不同timeType做初始化
    // 通常是用if else来区分timeType，也可以直接使用DefaultAppLike里面的4个辅助函数
    fun onCreate(context: Context?, timeType: Int)

    // onTerminate
    fun onTerminate()

    // onTerminate
    fun onLowMemory()

    // 进入后台
    fun onEnterBackground()

    // 回到前台
    fun onEnterForeground()
}