package com.haha.api

import com.google.auto.service.AutoService


/**
 *
 * @author : haha
 * @date   : 2024-09-03
 * @desc   : 测试
 * @version: 1.0
 *
 */
@AutoService(value = [IUserService::class])
class UserService: IUserService {
    override fun getUserName(): String {
        return "hahanihao"
    }
}