package com.haha.service.impl.service

/**
 *
 * @author : haha
 * @date   : 2024-09-14
 * @desc   :
 * @version: 1.0
 *
 */
interface IFactory {
    @Throws(Exception::class)
    fun <T> create(clazz: Class<T>?): T?
}