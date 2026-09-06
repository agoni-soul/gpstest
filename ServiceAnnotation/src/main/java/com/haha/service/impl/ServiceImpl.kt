package com.haha.service.impl

/**
 * APT / 调试用的服务元数据。运行时查表使用 [com.haha.service.impl.service.ServiceRecord]。
 */
class ServiceImpl {
    val key: String
    val implementation: String
    val implementationClazz: Class<*>?
    val isSingleton: Boolean

    companion object {
        private const val SPLITTER = ":"
        private const val SINGLETON = "singleton"
        const val DEFAULT_IMPL_KEY: String = "_service_default_impl"

        fun checkConflict(
            interfaceName: String?,
            previous: ServiceImpl?,
            incoming: ServiceImpl?
        ): String? {
            if (previous == null || incoming == null) {
                return null
            }
            if (previous.implementation == incoming.implementation) {
                return null
            }
            return if (DEFAULT_IMPL_KEY == incoming.key || DEFAULT_IMPL_KEY == previous.key) {
                String.format(
                    "接口%s 的默认实现只允许存在一个\n目前存在多个默认实现: %s, %s",
                    interfaceName,
                    previous,
                    incoming
                )
            } else {
                String.format(
                    "接口%s对应key='%s'存在多个实现: %s, %s",
                    interfaceName,
                    incoming.key,
                    previous,
                    incoming
                )
            }
        }
    }

    constructor(key: String?, implementation: Class<*>?, singleton: Boolean) {
        if (key == null || implementation == null) {
            throw RuntimeException("key和implementation不应该为空")
        }
        this.key = key
        this.implementation = implementation.name
        this.implementationClazz = implementation
        this.isSingleton = singleton
    }

    constructor(key: String?, implementation: String, singleton: Boolean) {
        if (implementation.isEmpty()) {
            throw RuntimeException("implementation不应该为空")
        }
        this.key = if (key.isNullOrEmpty()) implementation else key
        this.implementation = implementation
        this.implementationClazz = null
        this.isSingleton = singleton
    }

    fun toConfig(): String {
        var s = key + SPLITTER + implementation
        if (isSingleton) {
            s += SPLITTER + SINGLETON
        }
        return s
    }

    override fun toString(): String = implementation
}
