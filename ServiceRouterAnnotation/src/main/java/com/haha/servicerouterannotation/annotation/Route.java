package com.haha.servicerouterannotation.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

/**
 * @auther: haha
 * @Date: 2026/1/3
 * @Detail:
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Route {
    String path() default "";
    int priority() default 0;
    String name() default "";
    String pathPrefix() default "";
    String pathPattern() default "";
}
