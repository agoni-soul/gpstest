package com.haha.service.impl.utils

import com.haha.service.impl.service.DefaultFactory
import com.haha.service.impl.service.IFactory

/**
 *
 * @author : haha
 * @date   : 2024-09-14
 * @desc   :
 * @version: 1.0
 *
 */
object SingletonPool {
    private val CACHE: MutableMap<Class<*>, Any> = HashMap()

    @Throws(Exception::class)
    fun <I, T : I?> get(clazz: Class<I>?, factory: IFactory?): T? {
        var factory: IFactory? = factory
        if (clazz == null) {
            return null
        }
        if (factory == null) {
            factory = DefaultFactory.INSTANCE
        }
        val instance = getInstance(clazz, factory)
        return instance as? T
    }

    @Throws(Exception::class)
    private fun getInstance(clazz: Class<*>, factory: IFactory?): Any {
        var t = CACHE[clazz]
        if (t != null) {
            return t
        } else {
            synchronized(CACHE) {
                t = CACHE[clazz]
                if (t == null) {
                    t = factory?.create(clazz)
                    if (t != null) {
                        CACHE[clazz] = t!!
                    }
                }
            }
            return t!!
        }
    }
}