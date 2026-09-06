package com.haha.service.impl.utils

import com.haha.service.impl.service.DefaultFactory
import com.haha.service.impl.service.IFactory
import com.haha.service.impl.service.IServiceLifecycle
import com.haha.service.impl.service.InstanceCreator

object SingletonPool {
    private val CACHE: MutableMap<Class<*>, Any> = HashMap()

    @Throws(Exception::class)
    fun <I, T : I?> get(clazz: Class<I>?, factory: IFactory?): T? {
        if (clazz == null) {
            return null
        }
        val usedFactory = factory ?: DefaultFactory.INSTANCE
        val instance = getInstance(clazz, usedFactory) ?: return null
        @Suppress("UNCHECKED_CAST")
        return instance as? T
    }

    @Throws(Exception::class)
    private fun getInstance(clazz: Class<*>, factory: IFactory): Any? {
        val cached = CACHE[clazz]
        if (cached != null) {
            return cached
        }
        synchronized(CACHE) {
            val again = CACHE[clazz]
            if (again != null) {
                return again
            }
            val created = InstanceCreator.create(clazz, factory) ?: return null
            CACHE[clazz] = created
            return created
        }
    }

    fun clear() {
        synchronized(CACHE) {
            CACHE.values.forEach { value ->
                if (value is IServiceLifecycle) {
                    try {
                        value.onDestroy()
                    } catch (_: Exception) {
                    }
                }
            }
            CACHE.clear()
        }
    }
}
