package com.haha.service.impl

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
            impl: ServiceImpl?,
            previous: ServiceImpl?
        ): String? {
            if (impl != null && previous != null && !stringEquals(
                    previous.implementation,
                    impl.implementation
                )
            ) {
                return if (DEFAULT_IMPL_KEY == impl.key) {
                    String.format(
                        "接口%s 的默认实现只允许存在一个\n目前存在多个默认实现: %s, %s",
                        interfaceName,
                        previous,
                        impl
                    )
                } else {
                    String.format(
                        "接口%s对应key='%s'存在多个实现: %s, %s", interfaceName,
                        impl.key, previous, impl
                    )
                }
            }
            return null
        }

        private fun stringEquals(s1: String, s2: String): Boolean {
            return s1 == s2
        }

        private fun isEmpty(key: String?): Boolean {
            return key.isNullOrEmpty()
        }
    }

    constructor(key: String?, implementation: Class<*>?, singleton: Boolean) {
        if (key == null || implementation == null) {
            throw java.lang.RuntimeException("key和implementation不应该为空")
        }
        this.key = key
        this.implementation = ""
        this.implementationClazz = implementation
        this.isSingleton = singleton
    }

    constructor(key: String?, implementation: String, singleton: Boolean) {
        if (isEmpty(implementation)) {
            throw java.lang.RuntimeException("implementation不应该为空")
        }
        this.key = if (isEmpty(key)) implementation else key!! // 没有指定key，则为implementation
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

    override fun toString(): String {
        return implementation
    }
}