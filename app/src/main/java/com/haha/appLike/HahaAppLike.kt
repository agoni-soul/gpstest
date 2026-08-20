package com.haha.appLike

import android.app.Application
import android.content.Context
import com.haha.appLike.DefaultAppLike
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
class HahaAppLike: DefaultAppLike() {
    companion object {
        private val TAG = HahaAppLike::class.java.simpleName

        var application: Application? = null

        var homeScope: CoroutineScope? = null
    }

    override fun onCreate(context: Context?) {
        super.onCreate(context)
        application = context as Application?
        homeScope = MainScope()
    }

    override fun onTerminate() {
        super.onTerminate()
        homeScope?.cancel()
    }
}