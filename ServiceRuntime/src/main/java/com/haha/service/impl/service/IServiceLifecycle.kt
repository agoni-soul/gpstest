package com.haha.service.impl.service

/**
 * 服务实例生命周期。单例在首次创建时 [onCreate]，[com.haha.service.impl.utils.SingletonPool.clear] 时 [onDestroy]。
 */
interface IServiceLifecycle {
    fun onCreate()
    fun onDestroy()
}
