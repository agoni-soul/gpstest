package com.haha.service.impl.generated;

import com.haha.service.impl.core.Debugger;
import com.haha.service.impl.service.IServiceInit;

/**
 * 插件插桩点。ServiceRouterPlugin 在 {@link #loadServiceMap()} 末尾插入
 * {@code register("...ServiceInit_xxx")}。
 */
public final class ServiceLoaderInit {

    private ServiceLoaderInit() {
    }

    public static void init() {
        loadServiceMap();
    }

    /**
     * 插件插桩：在 RETURN 前插入 register(className)。
     */
    public static void loadServiceMap() {
        // auto generate register code by gradle plugin
        // register("com.haha.service.impl.generated.service.ServiceInit_ServiceImpl");
    }

    @SuppressWarnings("unused")
    public static void register(String className) {
        if (className == null || className.isEmpty()) {
            return;
        }
        try {
            Object obj = Class.forName(className).getDeclaredConstructor().newInstance();
            if (obj instanceof IServiceInit) {
                ((IServiceInit) obj).init();
                Debugger.d("Registered service init: %s", className);
            } else {
                Debugger.w("Not IServiceInit: %s", className);
            }
        } catch (Exception e) {
            Debugger.e(e);
            Debugger.e("register service init failed: %s", className);
        }
    }
}
