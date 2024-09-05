package com.haha.compiler

import android.util.Log
import com.google.auto.service.AutoService
import com.haha.api.Service
import com.haha.api.Service.TAG

/**
 *
 * @author : haha
 * @date   : 2024-09-05
 * @desc   :
 * @version: 1.0
 *
 */
@AutoService(Service::class)
class PythonService : Service {
    override fun start() {
        Log.d(TAG, "Loading PythonService service");
        println("Loading PythonService service");
    }

    override fun getUserName(): String = "PythonService"
}