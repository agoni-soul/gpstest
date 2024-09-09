package com.soul.main;

import android.util.Log;

import com.haha.processor.MConstants;

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

    private final static String TAG="bindUtils";

    public static void bind(Object target){
        Package p = target.getClass().getPackage();
        if (p == null) {
            Log.e(TAG, "Package == null");
            return;
        }
        Log.e(TAG, p.getName());
        String bindViewFile = target.getClass().getSimpleName().concat(MConstants._VIEW_BINDING);
        bindViewFile= p.getName().concat(".").concat(bindViewFile);

        Log.e(TAG,"bindView File Name:"+bindViewFile);

        try {
            Class<?> aClass = Class.forName(target.getClass().getSimpleName() + MConstants._VIEW_BINDING);
            Method inject = aClass.getDeclaredMethod(MConstants.INJECT_NAME, target.getClass());
            inject.invoke(aClass.newInstance(),target);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

}