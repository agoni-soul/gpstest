package com.haha.service.annotation.processor.processor;

public class ConstantUtils {

    private static final String PKG = "com.haha.service.impl.";

    public static final String GEN_PKG = PKG + "generated";
    public static final String GEN_PKG_SERVICE = GEN_PKG + ".service";
    public static final String SPLITTER = "_";

    public static final String SERVICE_LOADER_INIT = GEN_PKG + ".ServiceLoaderInit";
    public static final String INIT_METHOD = "init";

    public static final String SERVICE_LOADER_CLASS = PKG + "service.ServiceLoader";
    public static final String ISERVICE_INIT_CLASS = PKG + "service.IServiceInit";
    public static final String AWARE_CLASS = PKG + "service.IApplicationAware";
    public static final String LIFECYCLE_CLASS = PKG + "service.IServiceLifecycle";

    public static final String OPT_MODULE_NAME = "SERVICE_MODULE_NAME";
}
