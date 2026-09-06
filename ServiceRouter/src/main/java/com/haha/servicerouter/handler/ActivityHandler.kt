package com.haha.servicerouter.handler

import android.app.Activity
import android.app.Fragment
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat
import com.haha.servicerouter.core.DOFRouter
import com.haha.servicerouter.exceptions.RouteNotFoundException
import com.haha.servicerouterannotation.annotation.data.RouteMetaData
import com.haha.servicerouterutils.utils.Logger

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
internal class ActivityHandler(routeMetaData: RouteMetaData) : RouteHandler(routeMetaData) {

    override fun handle(context: Context, navigator: DOFRouter.Navigator): Any? {
        Logger.d("Handle by ActivityHandler, route to ${routeMetadata.clazz.simpleName}")
        val intent = Intent()
        val component = ComponentName(context, routeMetadata.clazz)
        intent.setComponent(component)
            .addFlags(navigator.flags)
            .putExtras(navigator.extras)

        try {
            when {
                navigator.resultLauncher != null -> {
                    navigator.resultLauncher!!.launch(intent)
                }

                navigator.activity != null -> {
                    startFromActivity(navigator.activity!!, intent, navigator)
                }

                navigator.fragment != null -> {
                    startFromFragment(navigator.fragment!!, intent, navigator)
                }

                navigator.fragmentX != null -> {
                    startFromFragmentX(navigator.fragmentX!!, intent, navigator)
                }

                navigator.context is Activity -> {
                    startFromActivity(navigator.context as Activity, intent, navigator)
                }

                else -> {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val tmpContext = navigator.context ?: context
                    ActivityCompat.startActivity(tmpContext, intent, navigator.options)
                    if (tmpContext is Activity) {
                        applyTransition(tmpContext, navigator)
                    }
                }
            }
        } catch (e: ActivityNotFoundException) {
            throw RouteNotFoundException(e)
        }
        return null
    }

    private fun startFromActivity(
        activity: Activity,
        intent: Intent,
        navigator: DOFRouter.Navigator
    ) {
        if (navigator.requestCode >= 0) {
            activity.startActivityForResult(intent, navigator.requestCode, navigator.options)
        } else {
            ActivityCompat.startActivity(activity, intent, navigator.options)
        }
        applyTransition(activity, navigator)
    }

    private fun startFromFragment(
        fragment: Fragment,
        intent: Intent,
        navigator: DOFRouter.Navigator
    ) {
        if (navigator.requestCode >= 0) {
            fragment.startActivityForResult(intent, navigator.requestCode, navigator.options)
        } else {
            fragment.startActivity(intent, navigator.options)
        }
    }

    private fun startFromFragmentX(
        fragment: androidx.fragment.app.Fragment,
        intent: Intent,
        navigator: DOFRouter.Navigator
    ) {
        if (navigator.requestCode >= 0) {
            fragment.startActivityForResult(intent, navigator.requestCode, navigator.options)
        } else {
            fragment.startActivity(intent, navigator.options)
        }
    }

    private fun applyTransition(activity: Activity, navigator: DOFRouter.Navigator) {
        if (navigator.enterAnim > -1 || navigator.exitAnim > -1) {
            activity.overridePendingTransition(navigator.enterAnim, navigator.exitAnim)
        }
    }
}
