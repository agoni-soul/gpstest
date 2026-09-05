package com.haha.servicerouter.core

import android.app.Application
import android.app.Fragment
import android.content.Context
import com.haha.servicerouter.exceptions.HandleException
import com.haha.servicerouter.handler.ActivityHandler
import com.haha.servicerouter.handler.FragmentHandler
import com.haha.servicerouter.handler.FragmentXHandler
import com.haha.servicerouter.handler.RouteHandler
import com.haha.servicerouter.handler.UnknownRouteHandler
import com.haha.servicerouter.interfaces.IInterceptorLoader
import com.haha.servicerouter.interfaces.IRouteInterceptor
import com.haha.servicerouter.interfaces.IRouteLoader
import com.haha.servicerouter.table.RouteTable
import com.haha.servicerouter.utils.ClassUtils
import com.haha.servicerouterannotation.annotation.RouteType
import com.haha.servicerouterannotation.annotation.data.RouteMetaData
import com.haha.servicerouterannotation.annotation.utils.Consts
import com.haha.servicerouterutils.utils.Logger

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
internal class Router private constructor() {
    
    private val TAG = "Router"

    internal lateinit var context: Context

    internal object Inner {
        val instance = Router()
    }

    companion object {

        fun getInstance() = Inner.instance

        fun init(context: Context) {
            getInstance().context = context as? Application ?: context.applicationContext
            getInstance().loadRouteTable()
        }
    }

    var registerByPlugin = false

    /**
     * 加载路由表
     */
    private fun loadRouteTable() {
        loadRouterMap()

        if (!registerByPlugin) {
            Logger.d("Load RouteTable by reflect")
            try {
                val routerMap = ClassUtils.getFileNameByPackageName(context, Consts.PACKAGE)
                if (!routerMap.isNullOrEmpty()) {
                    routerMap.forEach { className ->
                        when {
                            className.startsWith("${Consts.PACKAGE}.${Consts.ROUTE_LOADER_NAME}") -> {
                                Logger.d("Load route: $className")
                                (loadClassForName(className)?.newInstance() as? IRouteLoader)?.loadInto(
                                    RouteTable.routes
                                )
                            }

                            className.startsWith("${Consts.PACKAGE}.${Consts.INTERCEPTOR_LOADER_NAME}") -> {
                                Logger.d("Load interceptor: $className")
                                (loadClassForName(className)?.newInstance() as? IInterceptorLoader)
                                    ?.loadInto(RouteTable.interceptors)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e("Load RouteTable failed", e)
            }
        } else {
            Logger.d("Load RouteTable by Auto-Register")
        }
    }

    /**
     * 插件插桩点（对齐 ARouter LogisticsCenter.loadRouterMap）。
     * ServiceRouterPlugin 会在方法末尾插入 register("...RouteLoader_xxx")。
     */
    private fun loadRouterMap() {
        registerByPlugin = true
    }

    /**
     * 给 Gradle 插件插桩调用：按全类名实例化并分发到路由表 / 拦截器表。
     */
    @Suppress("unused")
    private fun register(className: String?) {
        if (className.isNullOrEmpty()) {
            return
        }
        try {
            val obj = Class.forName(className).getDeclaredConstructor().newInstance()
            when (obj) {
                is IRouteLoader -> registerRouteRoot(obj)
                is IInterceptorLoader -> registerInterceptor(obj)
                else -> Logger.w("Unknown register class: $className")
            }
        } catch (e: Exception) {
            Logger.e("register failed: $className", e)
        }
    }

    private fun registerRouteRoot(routeLoader: IRouteLoader?) {
        markRegisteredByPlugin()
        routeLoader?.let {
            Logger.d(  "Load route: ${it.javaClass.simpleName}")
            it.loadInto(RouteTable.routes)
        }
    }

    private fun registerInterceptor(interceptorLoader: IInterceptorLoader?) {
        markRegisteredByPlugin()
        interceptorLoader?.let {
            Logger.d(  "Load interceptor: ${it.javaClass.simpleName}")
            it.loadInto(RouteTable.interceptors)
        }
    }

    private fun markRegisteredByPlugin() {
        if (!registerByPlugin) {
            registerByPlugin = true
        }
    }

    private fun loadClassForName(className: String): Class<*>? {
        return try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            null
        }
    }

    /**
     * 发起路由请求
     * 若对应路由为fragment则返回，其余情况返回null
     */
    fun navigator(navigator: DOFRouter.Navigator): Any? {
        val isIntercept = isIntercept(navigator)
        if (isIntercept) {
            navigator.onInterceptCallback?.invoke(navigator)
            return null
        }

        val map = addressingComponent(navigator)

        if (map.isEmpty()) {
            Logger.w("${navigator.path} Not Found!")
            navigator.onNotFoundCallback?.invoke(navigator)
            return null
        }

        val handlers = createRouteHandler(map)
        handlers.forEach {
            navigator.onBeforeCallback?.invoke(navigator)
            try {
                val result = it.handle(context, navigator)
                navigator.onArrivedCallback?.invoke(navigator)
                if (result is Fragment || result is androidx.fragment.app.Fragment) {
                    return result
                }
            } catch (e: HandleException) {
                e.printStackTrace()
                navigator.onNotFoundCallback?.invoke(navigator)
            }
        }

        return null
    }

    /**
     * 开始寻址，从路由表中获取对应路径的路由
     * @return Map<String, RouteMetadata> key: 路由路径  value: 路由元数据
     */
    private fun addressingComponent(navigator: DOFRouter.Navigator): Map<String, RouteMetaData> {
        Logger.d(  "Addressing >> ${navigator.path}")
        return RouteTable.routes.filter { (path, meta) ->
            RouteTable.matchers.any { matcher ->
                matcher.match(path, navigator.path, meta)
            }
        }
    }

    /**
     * 将Map<String, RouteMetadata>转化为List<AbsRouteHandler>并且按照优先级进行排序
     * @return 返回路由处理者列表
     */
    private fun createRouteHandler(map: Map<String, RouteMetaData>): List<RouteHandler> {
        return map.map { createHandler(it.value) }
            .sortedWith(Comparator { o1, o2 -> o1.routeMetadata.priority - o2.routeMetadata.priority })
    }

    /**
     * 根据不同RouteType返回不同处理者
     */
    internal fun createHandler(routeMetaData: RouteMetaData): RouteHandler {
        return when (routeMetaData.routeType) {
            RouteType.ACTIVITY -> ActivityHandler(routeMetaData)
            RouteType.FRAGMENT -> FragmentHandler(routeMetaData)
            RouteType.FRAGMENT_X -> FragmentXHandler(routeMetaData)
            else -> UnknownRouteHandler(routeMetaData)
        }
    }

    /**
     * 执行拦截器
     * @return true:路由请求被拦截 false:该请求未被拦截
     */
    private fun isIntercept(navigator: DOFRouter.Navigator): Boolean {
        return RouteTable.interceptors.asSequence().find {
            try {
                val clazz = it.value.clazz
                val interceptor = clazz.newInstance() as IRouteInterceptor
                return@find if (interceptor.intercept(context, navigator)) {
                    Logger.d("Intercept by ${clazz.simpleName}")
                    true
                } else {
                    false
                }
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: ClassCastException) {
                e.printStackTrace()
            }
            return@find false
        } != null
    }
}