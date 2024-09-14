package com.haha.service.impl.service

import android.util.Log
import com.haha.service.annotation.processor.processor.ConstantUtils
import com.haha.service.impl.ServiceImpl
import com.haha.service.impl.utils.SingletonPool

/**
 *
 * @author : haha
 * @date   : 2024-09-14
 * @desc   :
 * @version: 1.0
 *
 */
open class ServiceLoader<I>(interfaceClass: Class<*>?) {
    companion object {
        private var mIsHasInit: Boolean = false

        private val SERVICES: MutableMap<Class<*>, ServiceLoader<*>> by lazy {
            HashMap()
        }

        fun lazyInit() {
            synchronized(this) {
                if (!mIsHasInit) {

                    try {
                        // 反射调用Init类，避免引用的类过多，导致main dex capacity exceeded问题
                        Class.forName(ConstantUtils.SERVICE_LOADER_INIT)
                            .getMethod(ConstantUtils.INIT_METHOD)
                            .invoke(null)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    mIsHasInit = true
                }
            }
        }

        /**
         * 提供给InitClass使用的初始化接口
         *
         * @param interfaceClass 接口类
         * @param implementClass 实现类
         */
        fun put(interfaceClass: Class<*>, key: String, implementClass: Class<*>, singleton: Boolean) {
            var loader = SERVICES[interfaceClass]
            if (loader == null) {
                loader = ServiceLoader<Any>(interfaceClass)
                SERVICES[interfaceClass] = loader
            }
            loader.putImpl(key, implementClass, singleton)
        }

        /**
         * 根据接口获取 [ServiceLoader]
         */
        fun <T> load(interfaceClass: Class<T>?): ServiceLoader<T>? {
            lazyInit()
            if (interfaceClass == null) {
                NullPointerException("ServiceLoader.load的class参数不应为空")
                return EmptyServiceLoader.INSTANCE
            }
            var service: ServiceLoader<T>? = SERVICES[interfaceClass] as? ServiceLoader<T>
            if (service == null) {
                synchronized(SERVICES) {
                    service = SERVICES[interfaceClass] as? ServiceLoader<T>
                    if (service == null) {
                        service = ServiceLoader(interfaceClass)
                        SERVICES[interfaceClass] = service!!
                    }
                }
            }
            return service
        }
    }

    private val mMap: HashMap<String, ServiceImpl> by lazy { HashMap() }

    private var mInterfaceName: String = ""

    init {
        if (interfaceClass == null) {
            mInterfaceName = ""
        } else {
            mInterfaceName = interfaceClass.name
        }
    }

    private fun putImpl(key: String?, implementClass: Class<*>?, singleton: Boolean) {
        key ?: return
        implementClass ?: return
        mMap[key] = ServiceImpl(key, implementClass, singleton)
    }

    /**
     * 创建指定key的实现类实例，使用 [IServiceProvider] 方法或无参数构造。对于声明了singleton的实现类，不会重复创建实例。
     *
     * @return 可能返回null
     */
    fun <T : I?> get(key: String?): T? {
        return createInstance<T>(mMap[key], null)
    }

    /**
     * 创建指定key的实现类实例，使用指定的Factory构造。对于声明了singleton的实现类，不会重复创建实例。
     *
     * @return 可能返回null
     */
    fun <T : I?> get(key: String?, factory: IFactory?): T? {
        return createInstance<T>(mMap[key], factory)
    }

    /**
     * 创建所有实现类的实例，使用 [IServiceProvider] 方法或无参数构造。对于声明了singleton的实现类，不会重复创建实例。
     *
     * @return 可能返回EmptyList，List中的元素不为空
     */
    fun <T : I?> getAll(): List<T> {
        return getAll<T>(null as IFactory?)
    }

    /**
     * 创建所有实现类的实例，使用指定Factory构造。对于声明了singleton的实现类，不会重复创建实例。
     *
     * @return 可能返回EmptyList，List中的元素不为空
     */
    open fun <T : I?> getAll(factory: IFactory?): List<T> {
        val services: Collection<ServiceImpl> = mMap.values
        if (services.isEmpty()) {
            return emptyList()
        }
        val list: MutableList<T> = ArrayList(services.size)
        for (impl in services) {
            val instance = createInstance<T>(impl, factory)
            if (instance != null) {
                list.add(instance)
            }
        }
        return list
    }

    /**
     * 获取指定key的实现类。注意，对于声明了singleton的实现类，获取Class后还是可以创建新的实例。
     *
     * @return 可能返回null
     */
    fun <T : I?> getClass(key: String?): Class<T>? {
        return mMap[key]?.implementationClazz as? Class<T>
    }

    /**
     * 获取所有实现类的Class。注意，对于声明了singleton的实现类，获取Class后还是可以创建新的实例。
     *
     * @return 可能返回EmptyList，List中的元素不为空
     */
    fun <T : I?> getAllClasses(): List<Class<T>> {
        val list: MutableList<Class<T>> = ArrayList(mMap.size)
        for (impl in mMap.values) {
            val clazz = impl.implementationClazz as Class<T>?
            if (clazz != null) {
                list.add(clazz)
            }
        }
        return list
    }

    private fun <T : I?> createInstance(impl: ServiceImpl?, factory: IFactory?): T? {
        var factory: IFactory? = factory
        if (impl == null) {
            return null
        }
        val clazz = impl.implementationClazz as Class<T>
        if (impl.isSingleton) {
            try {
                return SingletonPool.get(clazz, factory)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                if (factory == null) {
                    factory = DefaultFactory()
                }
                val t: T = factory.create(clazz)
//                Log.i("[ServiceLoader] create instance: %s, result = %s", clazz, t)
                return t
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    override fun toString(): String {
        return "ServiceLoader ($mInterfaceName)"
    }

    class EmptyServiceLoader : ServiceLoader<Any>(null) {
        val allClasses: List<Class<*>>
            get() = emptyList()

        val all: List<*>
            get() = emptyList<Any>()

//        override fun getAll(factory: IFactory?): MutableList<Any> {
//            return emptyList<Any>()
//        }

        override fun <Any> getAll(factory: IFactory?): List<Any> {
            return emptyList()
        }

        override fun toString(): String {
            return "EmptyServiceLoader"
        }

        companion object {
            val INSTANCE: ServiceLoader<Any> = EmptyServiceLoader()
        }
    }
}