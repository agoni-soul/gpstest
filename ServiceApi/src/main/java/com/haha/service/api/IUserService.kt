package com.haha.service.api

/**
 *
 * @author : haha
 * @date   : 2024-09-03
 * @desc   : 测试
 * @version: 1.0
 *
 */
//@IServiceLoader(interfaces = [IUserService::class], singleton = true, defaultImpl = true)
interface IUserService {
    fun getUserName(): String?

    fun start()
}