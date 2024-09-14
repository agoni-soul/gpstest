package com.haha.service.impl.api

import android.os.Looper
import com.haha.service.annotation.IServiceLoader
import com.haha.service.impl.service.ServiceLoader

/**
 *
 * @author : haha
 * @date   : 2024-09-14
 * @desc   : [IServiceLoader]注解实现帮助类
 * @version: 1.0
 *
 */
object ServiceLoaderHelper {
    fun init() {

        if (Looper.myLooper() != Looper.getMainLooper()) {
//            Debugger.fatal("初始化方法init应该在主线程调用")
        }
    }

    fun lazyInit() {
        ServiceLoader.lazyInit()
    }
}