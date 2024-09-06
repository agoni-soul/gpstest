package com.itbird.bindview.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解
 * 只在编译期间存在
 * Created by itbird on 2022/4/11
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface ItbirdAopBinderView {
    int value();
}
