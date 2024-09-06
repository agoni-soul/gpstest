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

    private final static String TAG="bindUtils";

    private final static String SUFFIX="_ViewBinding";

    public static void bind(Object target){
        Package p = target.getClass().getPackage();
        if (p == null) {
            Log.e(TAG, "Package == null");
            return;
        }
        Log.e(TAG, p.getName());
        String bindViewFile = target.getClass().getSimpleName().concat(SUFFIX);
        bindViewFile= p.getName().concat(".").concat(bindViewFile);

        Log.e(TAG,"bindView File Name:"+bindViewFile);

        try {
            Class<?> aClass = Class.forName(bindViewFile);
            Method inject = aClass.getDeclaredMethod("inject", target.getClass());
            inject.invoke(null,target);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

    }

}