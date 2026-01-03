package com.haha.servicerouterannotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @auther: haha
 * @Date: 2026/1/3
 * @Detail:
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Interceptor {
    int value() default 0;
    int priority() default 0;

    String name() default "";
}