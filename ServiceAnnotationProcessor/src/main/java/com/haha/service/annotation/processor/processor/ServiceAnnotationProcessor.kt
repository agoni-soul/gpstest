package com.haha.service.annotation.processor.processor

import com.google.auto.service.AutoService
import com.haha.service.annotation.IServiceLoader
import com.haha.service.impl.ServiceImpl
import com.sun.tools.javac.code.Symbol
import java.util.Collections
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror

/**
 *
 * @author : haha
 * @date   : 2024-09-14
 * @desc   : 定义IServiceLoader实现Processor类
 * @version: 1.0
 *
 */
@AutoService(Processor::class)
class ServiceAnnotationProcessor: BaseProcessor() {
    companion object {
        fun getInterface(service: IServiceLoader): MutableList<out TypeMirror?>? {
            try {
                service.interfaces
            } catch (mte: MirroredTypesException) {
                return mte.typeMirrors
            }
            return null
        }
    }

    private val mEntityMap: MutableMap<String, Entity> by lazy {
        mutableMapOf()
    }

    private var mHash: String? = null

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        if (roundEnv?.processingOver() != false) {
            generateInitClass()
        } else {
            processAnnotations(roundEnv)
        }
        return true
    }

    private fun generateInitClass() {
        if (mEntityMap.isEmpty() || mHash == null) {
            return
        }
        val generator = ServiceInitClassBuilder("ServiceInit${ConstantUtils.SPLITTER}$mHash")
        for (entry in mEntityMap.entries) {
            for (service in entry.value.map.values) {
                generator.put(entry.key, service.key, service.implementation, service.isSingleton)
            }
        }
        generator.build()
    }

    private fun processAnnotations(roundEnv: RoundEnvironment?) {
        roundEnv ?: return
        for (element in roundEnv.getElementsAnnotatedWith(IServiceLoader::class.java)) {
            if (element !is Symbol.ClassSymbol) {
                continue
            }
            if (mHash == null) {
                mHash = hash(element.className())
            }
            val service = element.getAnnotation(IServiceLoader::class.java) ?: continue
            val typeMirrors = getInterface(service)
            val keys = service.key

            val implementationName = element.className()
            val singleton = service.singleton
            val defaultImpl = service.defaultImpl

            if (!typeMirrors.isNullOrEmpty()) {
                for (mirror in typeMirrors) {
                    mirror ?: continue
                    if (!isConcreteSubType(element, mirror)) {
                        val msg =
                            "${element.className()}没有实现注解${IServiceLoader::class.java.name}标注的接口$mirror"
                        throw RuntimeException(msg)
                    }
                    val interfaceName = getClassName(mirror)
                    var entity = mEntityMap[interfaceName]
                    if (entity == null) {
                        entity = Entity(interfaceName)
                        mEntityMap[interfaceName] = entity
                    }

                    if (defaultImpl) {
                        //如果设置为默认实现，则手动添加一个内部标识默认实现的key
                        entity.put(ServiceImpl.DEFAULT_IMPL_KEY, implementationName, singleton)
                    }

                    if (keys.isNotEmpty()) {
                        for (key in keys) {
                            if (key.contains(":")) {
                                val msg = String.format(
                                    "%s: 注解%s的key参数不可包含冒号",
                                    implementationName, IServiceLoader::class.java.name
                                )
                                throw java.lang.RuntimeException(msg)
                            }
                            entity.put(key, implementationName, singleton)
                        }
                    } else {
                        entity.put(null, implementationName, singleton)
                    }
                }
            }
        }
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return HashSet(Collections.singletonList(IServiceLoader::class.java.name))
    }

    class Entity(private val mInterfaceName: String) {
        private val mMap: MutableMap<String, ServiceImpl> = HashMap()

        val map: Map<String, ServiceImpl>
            get() = mMap

        fun put(key: String?, implementationName: String?, singleton: Boolean) {
            implementationName ?: return
            val impl = ServiceImpl(key, implementationName, singleton)
            val prev: ServiceImpl? = mMap.put(impl.key, impl)
            val errorMsg: String? = ServiceImpl.checkConflict(mInterfaceName, prev, impl)
            if (errorMsg != null) {
                throw RuntimeException(errorMsg)
            }
        }

        val contents: List<String>
            get() {
                val list: MutableList<String> = ArrayList()
                for (impl in mMap.values) {
                    list.add(impl.toConfig())
                }
                return list
            }
    }
}

