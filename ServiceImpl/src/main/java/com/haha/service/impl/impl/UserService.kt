package com.haha.service.impl.impl

import android.util.Log
import com.google.auto.service.AutoService
import com.haha.service.annotation.IServiceLoader
import com.haha.service.api.IUserService


/**
 *
 * @author : haha
 * @date   : 2024-09-03
 * @desc   : 测试
 * @version: 1.0
 *
 */
//@AutoService(IUserService::class)
@IServiceLoader(interfaces = [IUserService::class], singleton = true, defaultImpl = true)
class UserService: IUserService {
    private val TAG = "Service"
    override fun getUserName(): String {
        return "hahanihao"
    }

    override fun start() {
        Log.d(TAG, "Loading user service")
    }
}