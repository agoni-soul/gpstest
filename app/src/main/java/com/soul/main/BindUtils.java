package com.soul.main;

import android.util.Log;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author:     haha
 * @date:       2024/9/6
 * Description: 自定义AutoService测试
 *
 **/


public class BindUtils {
    public static String INJECT_NAME = "bind";

    public static String _VIEW_BINDING = "_ViewBinding";

    private final static String TAG="bindUtils";

    public static void bind(Object activity){
        Package p = activity.getClass().getPackage();
        if (p == null) {
            Log.e(TAG, "Package == null");
            return;
        }
        Log.e(TAG, p.getName());
        String bindViewFile = activity.getClass().getSimpleName().concat(_VIEW_BINDING);
        bindViewFile= p.getName().concat(".").concat(bindViewFile);

        Log.e(TAG,"bindView File Name:"+bindViewFile);

        Class clazz = activity.getClass();
        Log.e(TAG, clazz.getName());
        Log.e(TAG, clazz.getSimpleName());
        try {
            Class bindViewClass = Class.forName(bindViewFile);
            Method method = bindViewClass.getMethod(INJECT_NAME, activity.getClass());
            method.invoke(bindViewClass.newInstance(), activity);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

//        try {
//            Class<?> aClass = Class.forName(bindViewFile);
//            Method inject = aClass.getDeclaredMethod(MConstants.INJECT_NAME, activity.getClass());
//            inject.invoke(aClass.newInstance(),activity);
//        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
//                 InvocationTargetException | InstantiationException e) {
//            e.printStackTrace();
//            Log.e(TAG, e.getMessage());
//        }
    }

}