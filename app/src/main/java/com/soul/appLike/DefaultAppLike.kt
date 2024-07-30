package com.soul.appLike

import android.content.Context


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
open class DefaultAppLike: IAppLike {

    override fun getPriority(): Int {
        return IAppLike.NORM_PRIORITY
    }

    override fun onCreate(context: Context?) {}

    override fun onCreate(context: Context?, timeType: Int) {
        if (timeType == IAppLike.TIME_TYPE_MAIN) {
            onCreateByMain(context)
        } else if (timeType == IAppLike.TIME_TYPE_THIRD_SDK) {
            onCreateByThirdSDK(context)
        } else if (timeType == IAppLike.TIME_TYPE_MAIN_DELAY) {
            onCreateByMainDelay(context)
        } else if (timeType == IAppLike.TIME_TYPE_SUB_DELAY) {
            onCreateBySubDelay(context)
        } else if (timeType == IAppLike.TIME_TYPE_NEED_AGREE_PRIVACY_SUB) {
            onCreateNeedAgreePrivacySub(context)
        } else if (timeType == IAppLike.TIME_TYPE_SYSTEM_IDLE_HANDLE) {
            onCreateSystemIdleHandle(context)
        }
    }

    fun onCreateByMain(context: Context?) {}

    fun onCreateByThirdSDK(context: Context?) {}

    fun onCreateByMainDelay(context: Context?) {}

    fun onCreateBySubDelay(context: Context?) {}

    fun onCreateNeedAgreePrivacySub(context: Context?) {}

    fun onCreateSystemIdleHandle(context: Context?) {}

    override fun onTerminate() {}

    override fun onLowMemory() {}

    override fun onEnterBackground() {}

    override fun onEnterForeground() {}
}