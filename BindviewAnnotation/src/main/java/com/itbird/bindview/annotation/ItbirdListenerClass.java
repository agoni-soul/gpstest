package com.itbird.bindview.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 注解的注解，用于注解onclick事件的数据
 * Created by itbird on 2022/4/19
 */
@Retention(CLASS)
@Target(ANNOTATION_TYPE)
public @interface ItbirdListenerClass {
    //class name : View.OnClickListener.class
    Class<?> targetClass();

    //listener inner'method name : onClick
    String methodName();

    //setListener method name : setOnClickListener
    String setListener();
}
