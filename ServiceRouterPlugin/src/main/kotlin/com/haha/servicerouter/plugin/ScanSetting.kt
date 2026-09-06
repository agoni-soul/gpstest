package com.haha.servicerouter.plugin

/**
 * 与 Router / ServiceLoader 运行时类名对齐，插件不能依赖 Android 模块。
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
    const val ROUTE_LOAD_INTO = "loadInto"
    const val ROUTE_LOAD_INTO_DESC = "(Ljava/util/Map;)V"
    const val ROUTE_MAP_PUT = "put"

    const val SERVICE_GENERATE_TO_CLASS = "com/haha/service/impl/generated/ServiceLoaderInit"
    const val SERVICE_GENERATE_TO_CLASS_FILE = "$SERVICE_GENERATE_TO_CLASS.class"
    const val SERVICE_GENERATE_TO_METHOD = "loadServiceMap"
    const val SERVICE_INIT_PACKAGE = "com/haha/service/impl/generated/service/"
    const val ISERVICE_INIT = "com/haha/service/impl/service/IServiceInit"
    const val SERVICE_LOADER_OWNER = "com/haha/service/impl/service/ServiceLoader"
    const val SERVICE_PUT_NAME = "put"
}
