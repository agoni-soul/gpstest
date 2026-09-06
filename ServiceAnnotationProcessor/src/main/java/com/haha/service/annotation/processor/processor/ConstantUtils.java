package com.haha.service.annotation.processor.processor;

import com.squareup.javapoet.ClassName;

public class ConstantUtils {


    public static ClassName CLASSNAME_LOG = ClassName.get("android.util", "Log");

    public static ClassName CLASSNAME_UI_THREAD = ClassName.get("android.support.annotation", "UiThread");

    public static ClassName CLASSNAME_VIEW = ClassName.get("android.view", "View");

    public static String INJECT_NAME = "bind";

    public static String _VIEW_BINDING = "_ViewBinding";

    private static final String PKG = "com.haha.service.impl.";

    public static final String NAME = "BASE_ANNOTATION";

    public static final String GEN_PKG = PKG + "generated";
    public static final String GEN_PKG_SERVICE = GEN_PKG + ".service";
    public static final String SPLITTER = "_";

    public static final String SERVICE_LOADER_INIT = GEN_PKG + ".ServiceLoaderInit";
    public static final char DOT = '.';
    public static final String INIT_METHOD = "init";

    public static final String URI_INTERCEPTOR_CLASS = PKG + "core.UriInterceptor";
    public static final String SERVICE_LOADER_CLASS = PKG + "service.ServiceLoader";
    public static final String ISERVICE_INIT_CLASS = PKG + "service.IServiceInit";
    public static final String AWARE_CLASS = PKG + "service.IApplicationAware";
    public static final String LIFECYCLE_CLASS = PKG + "service.IServiceLifecycle";

    public static final String OPT_MODULE_NAME = "SERVICE_MODULE_NAME";
}
