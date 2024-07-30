package com.soul.appLike

import android.app.Application
import android.content.Context
import com.soul.appLike.DefaultAppLike
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
class SoulAppLike: DefaultAppLike() {
    companion object {
        private val TAG = SoulAppLike::class.java.simpleName

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