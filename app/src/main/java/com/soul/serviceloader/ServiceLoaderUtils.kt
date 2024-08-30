package com.soul.serviceloader

import java.util.ServiceLoader

/**
 *
 * @author : haha
 * @date   : 2024-08-30
 * @desc   : ServiceLoader工具类
 * @version: 1.0
 *
 */
object ServiceLoaderUtils {
    fun <I> getService(clazz: Class<I>): I? {
        val service = ServiceLoader.load(clazz) ?: return null
        var count = 0
        var interfaceClass: I? = null
        service.forEach {
            count ++
            if (count >= 2) return@forEach
            interfaceClass = it
        }
        return if (count >= 2) {
            null
        } else {
            interfaceClass!!
        }
    }
}