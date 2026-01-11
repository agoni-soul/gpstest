package com.soul.binder

import android.app.Service
import android.content.Intent
import android.os.IBinder

//import com.soul.gpstest.IProcessStub

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