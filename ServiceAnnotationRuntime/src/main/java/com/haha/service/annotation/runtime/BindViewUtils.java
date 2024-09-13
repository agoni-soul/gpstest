package com.haha.service.annotation.runtime;

import com.haha.service.annotation.processor.ConstantUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 反射调用APT+javapoet生成的类
 * Created by itbird on 2022/4/11
 */
public class BindViewUtils {

    public static void bind(Object activity) {

        Class clazz = activity.getClass();
        try {
            Class bindViewClass = Class.forName(clazz.getName() + ConstantUtils._VIEW_BINDING);
            Method method = bindViewClass.getMethod(ConstantUtils.INJECT_NAME, activity.getClass());
            method.invoke(bindViewClass.newInstance(), activity);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
                 NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
