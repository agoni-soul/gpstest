package com.haha.service.loader

import android.util.Log
import com.haha.service.impl.ServiceImpl
import com.haha.service.impl.service.ServiceLoader

/**
 *
 * @author : haha
 * @date   : 2024-09-18
 * @desc   :
 * @version: 1.0
 *
 */
class ServiceLoaderHelper {

    companion object {
        private val TAG = "ServiceLoaderHelper"

        fun <T> loadService(clazz: Class<T>): ServiceLoader<T>? = ServiceLoader.load(clazz)

        fun <I, T: I?> getService(clazz: Class<I>?): I? {
            val service: I? = ServiceLoader.load(clazz)?.get(ServiceImpl.DEFAULT_IMPL_KEY) as? I
            Log.d(TAG, "service = $service")
            if (service != null) {
                return service
            } else {
                val services: List<I>? = getAllServices(clazz)
                if (services?.size == 1) {
                    return services[0]
                } else if ((services?.size ?: 0) > 1) {
                    // 打印日志
                }
            }
            return null
        }

        fun <I, T : I?> getAllServices(clazz: Class<I>?): List<T>? {
            return ServiceLoader.load(clazz)?.getAll()
        }
    }
}