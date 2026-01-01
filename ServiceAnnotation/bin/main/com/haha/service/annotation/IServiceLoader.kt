package com.haha.service.annotation

import kotlin.reflect.KClass

/**
 *
 * @author : haha
 * @date   : 2024-09-03
 * @desc   : 注解方法测试
 * @version: 1.0
 *
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class IServiceLoader(
    /**
     * 实现的接口（或继承的父类）
     */
    val interfaces: Array<KClass<*>>,
    /**
     * 同一个接口的多个实现类之间，可以通过唯一的key区分。
     */
    val key: Array<String> = [],
    /**
     * 是否为单例。如果是单例，则使用ServiceLoader.getService不会重复创建实例。
     */
    val singleton: Boolean = false,
    /**
     * 是否设置为默认实现类。如果是默认实现类，则在获取该实现类实例时可以不指定key
     */
    val defaultImpl: Boolean = false
)
