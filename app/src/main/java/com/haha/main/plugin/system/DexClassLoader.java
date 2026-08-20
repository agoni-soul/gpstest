package com.haha.main.plugin.system;

import java.io.File;

import dalvik.system.BaseDexClassLoader;

/**
 * @author: haha
 * @date: 2025/7/31
 * Description: 插件化的关键类
 **/
public class DexClassLoader extends BaseDexClassLoader {
    public DexClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, null, librarySearchPath, parent);
//        super((String)null, (File)null, (String)null, (ClassLoader) null);
//        throw new RuntimeException("Stub!");
    }
}
