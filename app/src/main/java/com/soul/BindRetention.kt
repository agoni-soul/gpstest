package com.soul

import java.lang.annotation.RetentionPolicy

/**
 *
 * @author : haha
 * @date   : 2024-09-03
 * @desc   : 注解方法测试
 * @version: 1.0
 *
 */
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
annotation class BindRetention(val value: Int = 0)
