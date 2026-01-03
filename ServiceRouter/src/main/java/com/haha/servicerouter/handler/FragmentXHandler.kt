package com.haha.servicerouter.handler

import android.content.Context
import androidx.fragment.app.Fragment
import com.haha.servicerouter.core.DOFRouter
import com.haha.servicerouter.exceptions.HandleException
import com.haha.servicerouter.exceptions.RouteNotFoundException
import com.haha.servicerouterannotation.annotation.data.RouteMetaData
import com.haha.servicerouterutils.utils.Logger

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
internal class FragmentXHandler(routeMetaData: RouteMetaData) : RouteHandler(routeMetaData) {

    override fun handle(context: Context, navigator: DOFRouter.Navigator): Any? {
        Logger.d("Handle by FragmentXHandler, route to ${routeMetadata.clazz.simpleName}")
        try {
            val clz = routeMetadata.clazz
            val fragment = clz.newInstance() as Fragment
            fragment.arguments = navigator.extras
            return fragment
        } catch (e: ClassNotFoundException) {
            throw RouteNotFoundException(e)
        } catch (e: ClassCastException) {
            throw HandleException(e)
        }
    }
}