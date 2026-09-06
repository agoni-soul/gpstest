package com.haha.service.impl.service

import android.app.Application
import android.content.Context
import com.haha.service.impl.ServiceImpl
import com.haha.service.impl.core.Debugger
import com.haha.service.impl.core.LogcatLogger
import com.haha.service.impl.service.ServiceLoader.Companion.lazyInit
import com.haha.service.impl.utils.ProcessUtils
import com.haha.service.impl.utils.SingletonPool
import java.util.concurrent.ConcurrentHashMap

/**
 * 按接口查找服务实现。注册表由 APT 生成的 [IServiceInit] 在 [lazyInit] 时写入。
 */
open class ServiceLoader<I> internal constructor() {

    internal val records: LinkedHashSet<ServiceRecord> = LinkedHashSet()
    internal val byKey: HashMap<String, ServiceRecord> = HashMap()
    internal var defaultRecord: ServiceRecord? = null

    companion object {
        private const val INIT_CLASS = "com.haha.service.impl.generated.ServiceLoaderInit"
        private val INIT_LOCK = Any()

        @Volatile
        private var initialized: Boolean = false

        private val SERVICES: ConcurrentHashMap<Class<*>, ServiceLoader<*>> = ConcurrentHashMap()

        @Volatile
        var application: Application? = null
            private set

        @JvmStatic
        @JvmOverloads
        fun init(context: Context, debug: Boolean = false) {
            application = context as? Application ?: context.applicationContext as? Application
            if (!Debugger.isLogSetting()) {
                Debugger.setLogger(LogcatLogger())
            }
            Debugger.setEnableDebug(debug)
            Debugger.setEnableLog(debug)
            lazyInit()
            if (debug) {
                Debugger.i(dump())
            }
        }

        @JvmStatic
        fun lazyInit() {
            if (initialized) {
                return
            }
            synchronized(INIT_LOCK) {
                if (initialized) {
                    return
                }
                try {
                    Class.forName(INIT_CLASS).getMethod("init").invoke(null)
                } catch (e: Exception) {
                    Debugger.e(e)
                    Debugger.e("ServiceLoaderInit.init failed")
                    if (Debugger.isEnableDebug()) {
                        throw e
                    }
                }
                initialized = true
            }
        }

        /**
         * 给生成类 / 插件调用的注册入口。
         */
        @JvmStatic
        fun put(
            interfaceClass: Class<*>,
            key: String?,
            implementClass: Class<*>,
            singleton: Boolean,
            defaultImpl: Boolean,
            priority: Int,
            process: String?
        ) {
            val processName = process.orEmpty()
            if (!ProcessUtils.isCurrentProcess(processName)) {
                Debugger.d(
                    "skip %s, process=%s current=%s",
                    implementClass.name,
                    processName,
                    ProcessUtils.currentProcessName()
                )
                return
            }
            val loader = SERVICES.getOrPut(interfaceClass) { ServiceLoader<Any>() }
            loader.putRecord(
                ServiceRecord(
                    interfaceClass = interfaceClass,
                    implClass = implementClass,
                    key = key.orEmpty(),
                    singleton = singleton,
                    isDefault = defaultImpl,
                    priority = priority
                )
            )
        }

        @JvmStatic
        fun <T : Any> load(interfaceClass: Class<T>): ServiceLoader<T> {
            requireNotNull(interfaceClass) { "ServiceLoader.load 的 class 参数不应为空" }
            lazyInit()
            @Suppress("UNCHECKED_CAST")
            return (SERVICES[interfaceClass] as? ServiceLoader<T>) ?: ServiceLoader()
        }

        @JvmStatic
        fun dump(): String {
            lazyInit()
            if (SERVICES.isEmpty()) {
                return "ServiceLoader: empty registry"
            }
            return buildString {
                append("ServiceLoader dump:\n")
                SERVICES.forEach { (iface, loader) ->
                    append("  ").append(iface.name).append('\n')
                    append("    default=").append(loader.defaultRecord?.implClass?.name)
                        .append('\n')
                    append("    keys=").append(loader.byKey.keys).append('\n')
                    loader.records.forEach { record ->
                        append("    - ").append(record.implClass.name)
                            .append(" key=").append(record.key)
                            .append(" singleton=").append(record.singleton)
                            .append(" default=").append(record.isDefault)
                            .append(" priority=").append(record.priority)
                            .append('\n')
                    }
                }
            }
        }
    }

    internal fun putRecord(record: ServiceRecord) {
        val existing = records.find { it.implClass == record.implClass }
        val merged = if (existing != null) {
            records.remove(existing)
            existing.copy(
                key = record.key.ifEmpty { existing.key },
                isDefault = existing.isDefault || record.isDefault,
                singleton = existing.singleton || record.singleton,
                priority = maxOf(existing.priority, record.priority)
            )
        } else {
            record
        }
        records.add(merged)

        if (merged.key.isNotEmpty()) {
            val previous = byKey.put(merged.key, merged)
            if (previous != null && previous.implClass != merged.implClass) {
                val msg =
                    "接口${merged.interfaceClass.name}对应key='${merged.key}'存在多个实现: ${previous.implClass.name}, ${merged.implClass.name}"
                Debugger.e(msg)
                if (Debugger.isEnableDebug()) {
                    throw IllegalStateException(msg)
                }
            }
        }
        if (merged.isDefault) {
            byKey.putIfAbsent(ServiceImpl.DEFAULT_IMPL_KEY, merged)
            val previous = defaultRecord
            if (previous != null && previous.implClass != merged.implClass) {
                val msg =
                    "接口${merged.interfaceClass.name} 的默认实现只允许存在一个: ${previous.implClass.name}, ${merged.implClass.name}"
                Debugger.e(msg)
                if (Debugger.isEnableDebug()) {
                    throw IllegalStateException(msg)
                }
            }
            defaultRecord = merged
        }
        Debugger.d(
            "put interface=%s key=%s impl=%s singleton=%s default=%s",
            merged.interfaceClass.name,
            merged.key,
            merged.implClass.name,
            merged.singleton,
            merged.isDefault
        )
    }

    fun <T : I?> getDefault(): T? = createInstance(defaultRecord)

    /**
     * @param key 业务 key；空或默认 key 时走默认实现。
     */
    fun <T : I?> get(key: String?): T? {
        if (key.isNullOrEmpty() || key == ServiceImpl.DEFAULT_IMPL_KEY) {
            return getDefault()
        }
        return createInstance(byKey[key])
    }

    fun <T : I?> get(key: String?, factory: IFactory?): T? {
        val record = if (key.isNullOrEmpty() || key == ServiceImpl.DEFAULT_IMPL_KEY) {
            defaultRecord
        } else {
            byKey[key]
        }
        return createInstance(record, factory)
    }

    fun <T : I?> getAll(): List<T> = getAll(null)

    open fun <T : I?> getAll(factory: IFactory?): List<T> {
        if (records.isEmpty()) {
            return emptyList()
        }
        return records
            .sortedByDescending { it.priority }
            .mapNotNull { createInstance<T>(it, factory) }
    }

    fun uniqueImplCount(): Int = records.map { it.implClass }.toSet().size

    fun hasImplementation(): Boolean = records.isNotEmpty()

    fun <T : I?> getClass(key: String?): Class<*>? {
        if (key.isNullOrEmpty() || key == ServiceImpl.DEFAULT_IMPL_KEY) {
            return defaultRecord?.implClass
        }
        return byKey[key]?.implClass
    }

    open fun <T : I?> getAllClasses(): List<Class<*>> {
        return records.map { it.implClass }.distinct()
    }

    fun <T : I?> getByImplClass(implClass: Class<*>): T? {
        val record = records.find { it.implClass == implClass } ?: return null
        return createInstance(record)
    }

    private fun <T : I?> createInstance(
        record: ServiceRecord?,
        factory: IFactory? = null
    ): T? {
        record ?: return null
        @Suppress("UNCHECKED_CAST")
        val clazz = record.implClass as? Class<T> ?: return null
        val usedFactory = factory ?: DefaultFactory.INSTANCE
        return try {
            if (record.singleton) {
                SingletonPool.get(clazz, usedFactory)
            } else {
                InstanceCreator.create(clazz, usedFactory)
            }
        } catch (e: Exception) {
            Debugger.e(e)
            Debugger.e("create %s failed", clazz.name)
            if (Debugger.isEnableDebug()) {
                throw ServiceCreateException(clazz.name, e)
            }
            null
        }
    }

    override fun toString(): String {
        return "ServiceLoader(size=${records.size}, default=${defaultRecord?.implClass?.simpleName})"
    }
}
