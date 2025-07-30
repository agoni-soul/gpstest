package com.soul.binder

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import com.soul.gpstest.IProcessStub

/**
 *
 * @author:     haha
 * @date:       2025/3/5
 * Description: 测试Service
 *
 **/
class TestService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return object : IProcessStub.Stub() {
            @Throws(RemoteException::class)
            override fun request(request: String?): String = "这是主线程"
        }
    }

}