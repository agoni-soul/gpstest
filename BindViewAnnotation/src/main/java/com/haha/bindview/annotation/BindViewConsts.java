package com.haha.bindview.annotation;

/**
 * APT 生成类后缀与注入方法名。Runtime 只依赖本模块，不依赖 Processor。
 */
public final class BindViewConsts {
    public static final String INJECT_NAME = "bind";
    public static final String VIEW_BINDING_SUFFIX = "_ViewBinding";

    private BindViewConsts() {
    }
}
