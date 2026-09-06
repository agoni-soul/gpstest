package com.haha.service.impl.service

object InstanceCreator {
    @Throws(Exception::class)
    fun <T> create(clazz: Class<T>, factory: IFactory): T? {
        val instance = factory.create(clazz) ?: return null
        val app = ServiceLoader.application
        if (instance is IApplicationAware && app != null) {
            instance.onAttach(app)
        }
        if (instance is IServiceLifecycle) {
            instance.onCreate()
        }
        return instance
    }
}
