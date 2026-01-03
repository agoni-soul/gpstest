package com.haha.servicerouter.handler

import android.app.Activity
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
                    navigator.activity!!.startActivityForResult(
                        intent,
                        navigator.requestCode,
                        navigator.options
                    )
                    if (navigator.enterAnim > -1 || navigator.exitAnim > -1) {
                        navigator.activity?.overridePendingTransition(
                            navigator.enterAnim,
                            navigator.exitAnim
                        )
                    }
                }
                navigator.fragment != null -> {
                    navigator.fragment?.startActivityForResult(
                        intent,
                        navigator.requestCode,
                        navigator.options
                    )
                }
                navigator.fragmentX != null -> {
                    navigator.fragmentX?.startActivityForResult(
                        intent,
                        navigator.requestCode,
                        navigator.options
                    )
                }
                navigator.context is Activity -> {
                    (navigator.context as Activity).startActivityForResult(
                        intent,
                        navigator.requestCode,
                        navigator.options
                    )
                    if (navigator.enterAnim > -1 || navigator.exitAnim > -1) {
                        (navigator.context as Activity).overridePendingTransition(
                            navigator.enterAnim,
                            navigator.exitAnim
                        )
                    }
                }
                else -> {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val tmpContext = navigator.context ?: context
                    ActivityCompat.startActivity(tmpContext, intent, navigator.options)
                    if (tmpContext is Activity && (navigator.enterAnim > -1 || navigator.exitAnim > -1)) {
                        tmpContext.overridePendingTransition(
                            navigator.enterAnim,
                            navigator.exitAnim
                        )
                    }
                }
            }
        } catch (e: ActivityNotFoundException) {
            throw RouteNotFoundException(e)
        }
        return null
    }
}