package com.haha.api

import com.haha.annotation.IServiceLoader
import kotlin.math.sin

/**
 *
 * @author : haha
 * @date   : 2024-09-03
 * @desc   : [IServiceLoader]注解实现类
 * @version: 1.0
 *
 */
class ServiceImpl {
    companion object {
        const val SINGLETON = "singleton"
        const val DEFAULT_SERVICE_IMPL_KEY = "default serviceImpl key"
    }

    var key: String? = null
        private set
    var implementation: String? = ""
        private set
    var implementationClazz: Class<*>? = null
        private set
    var singleton: Boolean = false
        private set

    constructor(key: String?, implementation: String?, singleton: Boolean) {
        if (implementation.isNullOrEmpty()) {
            throw RuntimeException("implementation 不应该为空")
        }
        this.key = if (key.isNullOrEmpty()) implementation else key
        this.implementation = implementation
        this.implementationClazz = null
        this.singleton = singleton
    }

    constructor(key: String?, implementationClazz: Class<*>?, singleton: Boolean) {
        if (key == null || implementationClazz == null) {
            throw RuntimeException("key和implementationClazz 不应该为空")
        }
        this.key = key
        this.implementation = ""
        this.implementationClazz = implementationClazz
        this.singleton = singleton
    }

    override fun toString(): String {
        return implementation ?: ""
    }
}