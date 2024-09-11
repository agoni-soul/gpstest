package com.haha.serviceImpl

import android.util.Log
import com.google.auto.service.AutoService
import com.haha.serviceApi.IUserService


/**
 *
 * @author : haha
 * @date   : 2024-09-03
 * @desc   : 测试
 * @version: 1.0
 *
 */
@AutoService(IUserService::class)
class UserService: IUserService {
    private val TAG = "Service"
    override fun getUserName(): String {
        return "hahanihao"
    }

    override fun start() {
        Log.d(TAG, "Loading user service")
    }
}