package com.haha.service.impl.service;


/**
 * 默认的Factory，先尝试Provider，再尝试无参数构造
 * <p>
 * Created by jzj on 2018/3/30.
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
//        T t = ProviderPool.create(clazz);
//        if (t != null) {
//            return t;
//        } else {
            return clazz.newInstance();
//        }
    }
}
