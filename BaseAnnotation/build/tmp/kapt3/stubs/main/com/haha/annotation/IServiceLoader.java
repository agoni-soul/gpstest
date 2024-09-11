package com.haha.annotation;

import java.lang.System;

/**
 * @author : haha
 * @date   : 2024-09-03
 * @desc   : 注解方法测试
 * @version: 1.0
 */
@kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u001b\n\u0000\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\b\u0087\u0002\u0018\u00002\u00020\u0001B6\u0012\u0010\u0010\u0002\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\u00040\u0003\u0012\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00060\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\bR\u000f\u0010\t\u001a\u00020\b\u00a2\u0006\u0006\u001a\u0004\b\t\u0010\nR\u0019\u0010\u0002\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\u00040\u0003\u00a2\u0006\u0006\u001a\u0004\b\u0002\u0010\u000bR\u0015\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00060\u0003\u00a2\u0006\u0006\u001a\u0004\b\u0005\u0010\fR\u000f\u0010\u0007\u001a\u00020\b\u00a2\u0006\u0006\u001a\u0004\b\u0007\u0010\n\u00a8\u0006\r"}, d2 = {"Lcom/haha/annotation/IServiceLoader;", "", "interfaces", "", "Lkotlin/reflect/KClass;", "key", "", "singleton", "", "defaultImpl", "()Z", "()[Ljava/lang/Class;", "()[Ljava/lang/String;", "BaseAnnotation"})
@java.lang.annotation.Target(value = {java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.TYPE_USE})
@java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.CLASS)
@kotlin.annotation.Retention(value = kotlin.annotation.AnnotationRetention.BINARY)
@kotlin.annotation.Target(allowedTargets = {kotlin.annotation.AnnotationTarget.TYPE, kotlin.annotation.AnnotationTarget.CLASS})
public abstract @interface IServiceLoader {
    
    /**
     * 实现的接口（或继承的父类）
     */
    public abstract java.lang.Class<?>[] interfaces();
    
    /**
     * 同一个接口的多个实现类之间，可以通过唯一的key区分。
     */
    public abstract java.lang.String[] key() default {};
    
    /**
     * 是否为单例。如果是单例，则使用ServiceLoader.getService不会重复创建实例。
     */
    public abstract boolean singleton() default false;
    
    /**
     * 是否设置为默认实现类。如果是默认实现类，则在获取该实现类实例时可以不指定key
     */
    public abstract boolean defaultImpl() default false;
}