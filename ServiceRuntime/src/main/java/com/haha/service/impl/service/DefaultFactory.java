package com.haha.service.impl.service;

import java.lang.reflect.Constructor;

/**
 * 默认 Factory：无参构造创建实例。
 */
public class DefaultFactory implements IFactory {

    public static final DefaultFactory INSTANCE = new DefaultFactory();

    DefaultFactory() {
    }

    @Override
    public <T> T create(Class<T> clazz) throws Exception {
        if (clazz == null) {
            throw new Exception("clazz is null");
        }
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
