package com.haha.servicerouterannotation.annotation.utils;

/**
 * @auther: haha
 * @Date: 2026/1/3
 * @Detail: 路由 APT / 运行时共用常量
 */
public final class Consts {
    public static final String TAG = "[DOFRouter:Processor]::";
    /**
     * 生成的 RouteLoader / InterceptorLoader 所在包，运行时按此包名扫 dex
     */
    public static final String PACKAGE = "com.haha.servicerouter.routes";
    public static final String ROUTE_LOADER_NAME = "RouteLoader";
    public static final String INTERCEPTOR_LOADER_NAME = "InterceptorLoader";
    /**
     * javac / kapt 传入的模块名 option key
     */
    public static final String MODULE_NAME = "DOFROUTER_MODULE_NAME";
    public static final String NO_MODULE_NAME_TIPS =
            "These no module name, at 'build.gradle', like :\n"
                    + "android {\n"
                    + "    defaultConfig {\n"
                    + "        javaCompileOptions {\n"
                    + "            annotationProcessorOptions {\n"
                    + "                arguments = [DOFROUTER_MODULE_NAME: project.getName()]\n"
                    + "            }\n"
                    + "        }\n"
                    + "    }\n"
                    + "}\n"
                    + "or kapt { arguments { arg(\"DOFROUTER_MODULE_NAME\", project.getName()) } }";
    public static final String INTERCEPTOR = "com.haha.servicerouter.interfaces.IRouteInterceptor";
    public static final String IROUTE_LOADER = "com.haha.servicerouter.interfaces.IRouteLoader";
    public static final String IINTERCEPTOR_LOADER = "com.haha.servicerouter.interfaces.IInterceptorLoader";
    public static final String METHOD_LOAD = "loadInto";

    /**
     * Gradle 插件 ASM 注入的目标类 / 方法
     */
    public static final String GENERATE_TO_CLASS_NAME = "com.haha.servicerouter.core.Router";
    public static final String GENERATE_TO_METHOD_NAME = "loadRouterMap";
    public static final String REGISTER_METHOD_NAME = "register";

    private Consts() {
    }
}
