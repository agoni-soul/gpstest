package com.haha.service.annotation.processor.processor

import com.google.auto.service.AutoService
import com.haha.service.annotation.IServiceLoader
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

@AutoService(Processor::class)
class ServiceAnnotationProcessor : BaseProcessor() {

    companion object {
        fun readInterfaces(service: IServiceLoader): List<TypeMirror> {
            return try {
                service.interfaces
                emptyList()
            } catch (mte: MirroredTypesException) {
                mte.typeMirrors.filterNotNull()
            }
        }
    }

    private val mEntityMap: MutableMap<String, Entity> = LinkedHashMap()

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedOptions(): MutableSet<String> {
        return mutableSetOf(ConstantUtils.OPT_MODULE_NAME)
    }

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
        if (mEntityMap.isEmpty()) {
            return
        }
        mEntityMap.values.forEach { it.markUniqueAsDefault() }
        val moduleName = sanitizeModuleName(
            mOptions?.get(ConstantUtils.OPT_MODULE_NAME) ?: "Default"
        )
        val generator = ServiceInitClassBuilder("ServiceInit${ConstantUtils.SPLITTER}$moduleName")
        for (entry in mEntityMap.entries) {
            for (spec in entry.value.impls.values) {
                generator.put(
                    entry.key,
                    spec.key,
                    spec.implementation,
                    spec.singleton,
                    spec.defaultImpl,
                    spec.priority,
                    spec.process
                )
            }
        }
        generator.build()
    }

    private fun processAnnotations(roundEnv: RoundEnvironment?) {
        roundEnv ?: return
        for (element in roundEnv.getElementsAnnotatedWith(IServiceLoader::class.java)) {
            if (element.kind != ElementKind.CLASS || element !is TypeElement) {
                continue
            }
            validateImplementation(element)
            val service = element.getAnnotation(IServiceLoader::class.java) ?: continue
            val typeMirrors = resolveInterfaces(element, service)
            if (typeMirrors.isEmpty()) {
                error(
                    element,
                    "${element.qualifiedName} 未声明 interfaces，且无法推断业务接口"
                )
                continue
            }

            val implementationName = element.qualifiedName.toString()
            val key = service.key
            if (key.contains(":")) {
                error(element, "$implementationName: 注解 IServiceLoader 的 key 不可包含冒号")
                continue
            }

            for (mirror in typeMirrors) {
                if (!isConcreteSubType(element, mirror)) {
                    error(
                        element,
                        "${element.qualifiedName} 没有实现注解 IServiceLoader 标注的接口 $mirror"
                    )
                    continue
                }
                val interfaceName = getClassName(mirror)
                val entity = mEntityMap.getOrPut(interfaceName) { Entity(interfaceName) }
                entity.add(
                    ImplSpec(
                        implementation = implementationName,
                        key = key,
                        singleton = service.singleton,
                        defaultImpl = service.defaultImpl,
                        priority = service.priority,
                        process = service.process
                    )
                ) { msg -> error(element, msg) }
            }
        }
    }

    private fun resolveInterfaces(
        element: TypeElement,
        service: IServiceLoader
    ): List<TypeMirror> {
        val declared = readInterfaces(service)
        if (declared.isNotEmpty()) {
            return declared
        }
        return element.interfaces.filter { isServiceInterface(it) }
    }

    private fun isServiceInterface(mirror: TypeMirror): Boolean {
        val name = mirror.toString()
        if (name.startsWith("java.") ||
            name.startsWith("javax.") ||
            name.startsWith("kotlin.") ||
            name.startsWith("android.") ||
            name.startsWith("androidx.")
        ) {
            return false
        }
        return name != ConstantUtils.AWARE_CLASS && name != ConstantUtils.LIFECYCLE_CLASS
    }

    private fun validateImplementation(element: TypeElement) {
        if (!element.modifiers.contains(Modifier.PUBLIC)) {
            error(element, "${element.qualifiedName} 必须是 public")
        }
        if (element.modifiers.contains(Modifier.ABSTRACT)) {
            error(element, "${element.qualifiedName} 不能是抽象类")
        }
        if (element.nestingKind.isNested && !element.modifiers.contains(Modifier.STATIC)) {
            error(element, "${element.qualifiedName} 若为内部类必须是 static")
        }
        val hasNoArg = element.enclosedElements.any { enclosed ->
            enclosed is ExecutableElement &&
                    enclosed.kind == ElementKind.CONSTRUCTOR &&
                    enclosed.parameters.isEmpty() &&
                    enclosed.modifiers.contains(Modifier.PUBLIC)
        }
        if (!hasNoArg && element.enclosedElements.none { it.kind == ElementKind.CONSTRUCTOR }) {
            // Kotlin 隐式构造
            return
        }
        if (!hasNoArg) {
            error(element, "${element.qualifiedName} 需要 public 无参构造")
        }
    }

    private fun sanitizeModuleName(raw: String): String {
        val sanitized = raw.replace(Regex("[^A-Za-z0-9_]"), "_")
        return sanitized.ifEmpty { "Default" }
    }

    private fun error(element: TypeElement, msg: String) {
        mMessager?.printMessage(Diagnostic.Kind.ERROR, msg, element)
        throw RuntimeException(msg)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return hashSetOf(IServiceLoader::class.java.name)
    }

    class ImplSpec(
        val implementation: String,
        val key: String,
        val singleton: Boolean,
        var defaultImpl: Boolean,
        val priority: Int,
        val process: String
    )

    class Entity(private val interfaceName: String) {
        val impls: LinkedHashMap<String, ImplSpec> = LinkedHashMap()

        fun add(spec: ImplSpec, onError: (String) -> Unit) {
            val existing = impls[spec.implementation]
            if (existing != null) {
                if (existing.key.isNotEmpty() && spec.key.isNotEmpty() && existing.key != spec.key) {
                    onError("${interfaceName}: ${spec.implementation} 注册了多个不同 key")
                    return
                }
                existing.defaultImpl = existing.defaultImpl || spec.defaultImpl
                return
            }
            if (spec.key.isNotEmpty() && impls.values.any { it.key == spec.key }) {
                onError("${interfaceName}: key='${spec.key}' 存在多个实现")
                return
            }
            if (spec.defaultImpl && impls.values.any { it.defaultImpl }) {
                onError("${interfaceName}: 默认实现只允许存在一个")
                return
            }
            impls[spec.implementation] = spec
        }

        fun markUniqueAsDefault() {
            if (impls.size == 1) {
                impls.values.first().defaultImpl = true
            }
        }
    }
}
