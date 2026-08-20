package com.haha.binder

import android.app.Service
import android.content.Intent
import android.os.IBinder

//import com.haha.gpstest.IProcessStub

/**
 *
 * @author:     haha
 * @date:       2025/3/5
 * Description: 测试Service
 *
 **/
class TestService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}