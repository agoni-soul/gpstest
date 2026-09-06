package com.haha.bindview.runtime;

import com.haha.bindview.annotation.BindViewConsts;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 反射调用 BindView APT 生成的 {@code Xxx_ViewBinding.bind(target)}。
 */
public class BindViewUtils {

    public static void bind(Object activity) {
        Class<?> clazz = activity.getClass();
        try {
            Class<?> bindViewClass = Class.forName(clazz.getName() + BindViewConsts.VIEW_BINDING_SUFFIX);
            Method method = bindViewClass.getMethod(BindViewConsts.INJECT_NAME, activity.getClass());
            method.invoke(bindViewClass.getDeclaredConstructor().newInstance(), activity);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
                 NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
