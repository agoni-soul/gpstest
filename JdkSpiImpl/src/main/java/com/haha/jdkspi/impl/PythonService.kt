package com.haha.jdkspi.impl

import android.util.Log
import com.google.auto.service.AutoService
import com.haha.jdkspi.api.JdkService

@AutoService(JdkService::class)
class PythonService : JdkService {
    override fun start() {
        Log.d(JdkService.TAG, "Loading PythonService service")
        println("Loading PythonService service")
    }

    override fun getUserName(): String = "PythonService"
}
