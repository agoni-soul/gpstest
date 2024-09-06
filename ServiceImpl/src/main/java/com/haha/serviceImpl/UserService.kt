package com.haha.serviceImpl

import android.util.Log
import com.google.auto.service.AutoService
import com.haha.api.IUserService


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
        Log.d(TAG, "hahanihao")
        return "hahanihao"
    }

    override fun start() {
        Log.d(TAG, "Loading user service")
    }
}