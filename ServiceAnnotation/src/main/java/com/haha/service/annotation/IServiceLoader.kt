package com.haha.service.annotation

import kotlin.reflect.KClass

/**
 * 组件化服务注册。编译期由 APT 写入 [com.haha.service.impl.service.IServiceInit]，
 * 打包期由插件聚合，运行时按接口 / key 查找。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class IServiceLoader(
    /**
     * 对外暴露的接口。为空时推断该类实现的业务接口（排除 java/kotlin/android 以及框架感知接口）。
     */
    val interfaces: Array<KClass<*>> = [],
    /**
     * 同一接口多个实现时的区分键。空表示无业务 key。
     */
    val key: String = "",
    /**
     * 是否单例。组件服务默认单例。
     */
    val singleton: Boolean = true,
    /**
     * 是否为默认实现。未指定且该接口在本模块只有一个实现时，APT 会自动标为默认。
     */
    val defaultImpl: Boolean = false,
    /**
     * 优先级，数值越大越靠前（[com.haha.service.impl.service.ServiceLoader.getAll] 排序）。
     */
    val priority: Int = 0,
    /**
     * 限定进程。空表示所有进程；`:push` 表示仅该后缀进程。
     */
    val process: String = ""
)
