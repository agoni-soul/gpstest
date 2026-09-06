package com.haha.service.loader

import com.haha.service.impl.core.Debugger
import com.haha.service.impl.service.ServiceLoader
import com.haha.service.impl.service.ServiceNotFoundException

/**
 * 组件服务门面。业务侧只通过这里按接口取实现。
 */
object ServiceLoaderHelper {

    @JvmStatic
    fun <I : Any> getService(clazz: Class<I>): I? {
        val loader = ServiceLoader.load(clazz)
        val defaultImpl = loader.getDefault<I>()
        if (defaultImpl != null) {
            return defaultImpl
        }
        val classes = loader.getAllClasses<I>()
        val result = when (classes.size) {
            1 -> loader.getByImplClass<I>(classes[0])
            0 -> {
                Debugger.w("No impl: %s", clazz.name)
                null
            }

            else -> {
                Debugger.w(
                    "Multiple impls for %s: %s, specify key or defaultImpl",
                    clazz.name,
                    classes.joinToString { it.name }
                )
                null
            }
        }
        if (result == null && Debugger.isEnableDebug()) {
            throw ServiceNotFoundException(clazz.name, "default missing, implCount=${classes.size}")
        }
        return result
    }

    @JvmStatic
    fun <I : Any> getService(clazz: Class<I>, key: String): I? {
        val service = ServiceLoader.load(clazz).get<I>(key)
        if (service == null) {
            Debugger.w("No impl: %s key=%s", clazz.name, key)
            if (Debugger.isEnableDebug()) {
                throw ServiceNotFoundException(clazz.name, "key=$key")
            }
        }
        return service
    }

    @JvmStatic
    fun <I : Any> requireService(clazz: Class<I>): I {
        return getService(clazz) ?: throw ServiceNotFoundException(clazz.name)
    }

    @JvmStatic
    fun <I : Any> requireService(clazz: Class<I>, key: String): I {
        return getService(clazz, key) ?: throw ServiceNotFoundException(clazz.name, "key=$key")
    }

    @JvmStatic
    fun <I : Any> getAllServices(clazz: Class<I>): List<I> {
        return ServiceLoader.load(clazz).getAll()
    }

    @JvmStatic
    fun <I : Any> hasService(clazz: Class<I>): Boolean {
        return ServiceLoader.load(clazz).hasImplementation()
    }

    inline fun <reified I : Any> getService(): I? = getService(I::class.java)

    inline fun <reified I : Any> getService(key: String): I? = getService(I::class.java, key)

    inline fun <reified I : Any> requireService(): I = requireService(I::class.java)

    inline fun <reified I : Any> getAllServices(): List<I> = getAllServices(I::class.java)
}
