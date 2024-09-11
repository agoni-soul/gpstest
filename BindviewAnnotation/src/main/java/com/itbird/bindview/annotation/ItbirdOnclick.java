package com.itbird.bindview.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Created by itbird on 2022/4/19
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
//@ItbirdListenerClass(targetClass = View.OnClickListener.class, setListener = "setOnClickListener", methodName = "onClick")
public @interface ItbirdOnclick {
    //返回注册的事件view的，id数组
    int[] value();
}
