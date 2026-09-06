package com.haha.jdkspi

import java.util.ServiceLoader

/**
 * JDK SPI 查找：恰好一个实现时返回，多于一个返回 null。
 */
object JdkServiceLoaderUtils {
    fun <I> getService(clazz: Class<I>): I? {
        val loader = ServiceLoader.load(clazz) ?: return null
        var count = 0
        var found: I? = null
        loader.forEach {
            count++
            if (count >= 2) {
                return@forEach
            }
            found = it
        }
        return if (count == 1) found else null
    }
}
