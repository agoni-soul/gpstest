package com.haha.servicerouter.plugin

/**
 * 与 [com.haha.servicerouterannotation.annotation.utils.Consts] 对齐，
 * 插件不能依赖 Android 模块，故在此写死。
 */
internal object ScanSetting {
    const val GENERATE_TO_CLASS = "com/haha/servicerouter/core/Router"
    const val GENERATE_TO_CLASS_FILE = "$GENERATE_TO_CLASS.class"
    const val GENERATE_TO_METHOD = "loadRouterMap"
    const val REGISTER_METHOD = "register"
    const val REGISTER_METHOD_DESC = "(Ljava/lang/String;)V"

    const val ROUTES_PACKAGE = "com/haha/servicerouter/routes/"
    const val IROUTE_LOADER = "com/haha/servicerouter/interfaces/IRouteLoader"
    const val IINTERCEPTOR_LOADER = "com/haha/servicerouter/interfaces/IInterceptorLoader"
}
