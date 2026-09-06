package com.haha.servicerouter.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * 扫描全部 CLASSES：
 * 1. 收集 Route / Interceptor Loader，改写 Router.loadRouterMap
 * 2. 收集 IServiceInit，改写 ServiceLoaderInit.loadServiceMap，并检查全局 key / 默认实现冲突
 */
abstract class RouterRegisterTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun taskAction() {
        val routeLoaders = linkedSetOf<String>()
        val interceptorLoaders = linkedSetOf<String>()
        val serviceInits = linkedSetOf<String>()
        val servicePuts = mutableListOf<ServicePut>()
        val written = hashSetOf<String>()

        collectFromInputs(routeLoaders, interceptorLoaders, serviceInits, servicePuts)
        checkServiceConflicts(servicePuts)

        logger.lifecycle("[DOFRouter] Auto-register routes: $routeLoaders")
        logger.lifecycle("[DOFRouter] Auto-register interceptors: $interceptorLoaders")
        logger.lifecycle("[DOFService] Auto-register inits: $serviceInits")

        val routerInject = ArrayList<String>(routeLoaders.size + interceptorLoaders.size).apply {
            addAll(routeLoaders)
            addAll(interceptorLoaders)
        }

        JarOutputStream(BufferedOutputStream(FileOutputStream(output.get().asFile))).use { jos ->
            allJars.get().forEach { file ->
                copyJar(file.asFile, jos, written, routerInject, serviceInits)
            }
            allDirectories.get().forEach { dir ->
                copyDirectory(dir.asFile, jos, written, routerInject, serviceInits)
            }
        }
    }

    private fun collectFromInputs(
        routeLoaders: MutableSet<String>,
        interceptorLoaders: MutableSet<String>,
        serviceInits: MutableSet<String>,
        servicePuts: MutableList<ServicePut>
    ) {
        allJars.get().forEach { file ->
            JarFile(file.asFile).use { jar ->
                jar.entries().asSequence()
                    .filter { !it.isDirectory && it.name.endsWith(".class") }
                    .forEach { entry ->
                        classifyLoader(
                            entry.name,
                            jar.getInputStream(entry),
                            routeLoaders,
                            interceptorLoaders,
                            serviceInits,
                            servicePuts
                        )
                    }
            }
        }
        allDirectories.get().forEach { dir ->
            dir.asFile.walkTopDown()
                .filter { it.isFile && it.extension == "class" }
                .forEach { classFile ->
                    val relative = classFile.relativeTo(dir.asFile).invariantSeparatorsPath
                    classifyLoader(
                        relative,
                        classFile.inputStream(),
                        routeLoaders,
                        interceptorLoaders,
                        serviceInits,
                        servicePuts
                    )
                }
        }
    }

    private fun classifyLoader(
        entryName: String,
        input: InputStream,
        routeLoaders: MutableSet<String>,
        interceptorLoaders: MutableSet<String>,
        serviceInits: MutableSet<String>,
        servicePuts: MutableList<ServicePut>
    ) {
        val isRoute = entryName.startsWith(ScanSetting.ROUTES_PACKAGE)
        val isService = entryName.startsWith(ScanSetting.SERVICE_INIT_PACKAGE)
        if ((!isRoute && !isService) || !entryName.endsWith(".class")) {
            input.close()
            return
        }
        input.use { stream ->
            val skip =
                if (isService) 0 else ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
            val reader = ClassReader(stream)
            var className: String? = null
            var interfaces: List<String> = emptyList()
            reader.accept(object : ClassVisitor(Opcodes.ASM9) {
                override fun visit(
                    version: Int,
                    access: Int,
                    name: String,
                    signature: String?,
                    superName: String?,
                    ifaces: Array<out String>?
                ) {
                    className = name
                    interfaces = ifaces?.toList() ?: emptyList()
                }

                override fun visitMethod(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    exceptions: Array<out String>?
                ): MethodVisitor? {
                    if (!isService || name != "init" || descriptor != "()V") {
                        return null
                    }
                    if (access and Opcodes.ACC_STATIC != 0) {
                        return null
                    }
                    return PutCollector(servicePuts)
                }
            }, skip)
            val name = className ?: return
            val dotted = name.replace('/', '.')
            when {
                interfaces.contains(ScanSetting.IROUTE_LOADER) -> routeLoaders.add(dotted)
                interfaces.contains(ScanSetting.IINTERCEPTOR_LOADER) -> interceptorLoaders.add(
                    dotted
                )

                interfaces.contains(ScanSetting.ISERVICE_INIT) -> serviceInits.add(dotted)
            }
        }
    }

    private fun checkServiceConflicts(puts: List<ServicePut>) {
        val byKey = linkedMapOf<String, ServicePut>()
        val byDefault = linkedMapOf<String, ServicePut>()
        puts.forEach { put ->
            if (put.key.isNotEmpty()) {
                val mapKey = "${put.interfaceName}#${put.key}"
                val prev = byKey.put(mapKey, put)
                if (prev != null && prev.implName != put.implName) {
                    throw GradleException(
                        "Service key conflict: interface=${put.interfaceName} key=${put.key} " +
                                "${prev.implName} vs ${put.implName}"
                    )
                }
            }
            if (put.defaultImpl) {
                val prev = byDefault.put(put.interfaceName, put)
                if (prev != null && prev.implName != put.implName) {
                    throw GradleException(
                        "Service defaultImpl conflict: interface=${put.interfaceName} " +
                                "${prev.implName} vs ${put.implName}"
                    )
                }
            }
        }
    }

    private fun copyJar(
        jarFile: File,
        jos: JarOutputStream,
        written: MutableSet<String>,
        routerInject: List<String>,
        serviceInits: Collection<String>
    ) {
        JarFile(jarFile).use { jar ->
            jar.entries().asSequence().forEach { entry ->
                if (entry.isDirectory || !written.add(entry.name)) {
                    return@forEach
                }
                jar.getInputStream(entry).use { input ->
                    val bytes = when (entry.name) {
                        ScanSetting.GENERATE_TO_CLASS_FILE ->
                            injectInstanceRegister(input.readBytes(), routerInject)

                        ScanSetting.SERVICE_GENERATE_TO_CLASS_FILE ->
                            injectStaticRegister(input.readBytes(), serviceInits)

                        else -> input.readBytes()
                    }
                    writeEntry(jos, entry.name, bytes)
                }
            }
        }
    }

    private fun copyDirectory(
        dir: File,
        jos: JarOutputStream,
        written: MutableSet<String>,
        routerInject: List<String>,
        serviceInits: Collection<String>
    ) {
        dir.walkTopDown().filter { it.isFile }.forEach { file ->
            val relative = file.relativeTo(dir).invariantSeparatorsPath
            if (!written.add(relative)) {
                return@forEach
            }
            val bytes = when (relative) {
                ScanSetting.GENERATE_TO_CLASS_FILE ->
                    injectInstanceRegister(file.readBytes(), routerInject)

                ScanSetting.SERVICE_GENERATE_TO_CLASS_FILE ->
                    injectStaticRegister(file.readBytes(), serviceInits)

                else -> file.readBytes()
            }
            writeEntry(jos, relative, bytes)
        }
    }

    private fun writeEntry(jos: JarOutputStream, name: String, bytes: ByteArray) {
        jos.putNextEntry(JarEntry(name))
        jos.write(bytes)
        jos.closeEntry()
    }

    private fun injectInstanceRegister(origin: ByteArray, injectNames: List<String>): ByteArray {
        if (injectNames.isEmpty()) {
            logger.lifecycle("[DOFRouter] no loader found, keep original loadRouterMap")
            return origin
        }
        val rewritten = rewriteMethod(
            origin,
            ScanSetting.GENERATE_TO_METHOD
        ) { mv, className ->
            mv.visitVarInsn(Opcodes.ALOAD, 0)
            mv.visitLdcInsn(className)
            mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                ScanSetting.GENERATE_TO_CLASS,
                ScanSetting.REGISTER_METHOD,
                ScanSetting.REGISTER_METHOD_DESC,
                false
            )
        }
        logger.lifecycle("[DOFRouter] injected ${injectNames.size} register() into Router.loadRouterMap")
        return rewritten(injectNames)
    }

    private fun injectStaticRegister(
        origin: ByteArray,
        injectNames: Collection<String>
    ): ByteArray {
        if (injectNames.isEmpty()) {
            logger.lifecycle("[DOFService] no IServiceInit found, keep original loadServiceMap")
            return origin
        }
        val rewritten = rewriteMethod(
            origin,
            ScanSetting.SERVICE_GENERATE_TO_METHOD
        ) { mv, className ->
            mv.visitLdcInsn(className)
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                ScanSetting.SERVICE_GENERATE_TO_CLASS,
                ScanSetting.REGISTER_METHOD,
                ScanSetting.REGISTER_METHOD_DESC,
                false
            )
        }
        logger.lifecycle("[DOFService] injected ${injectNames.size} register() into ServiceLoaderInit.loadServiceMap")
        return rewritten(injectNames.toList())
    }

    private fun rewriteMethod(
        origin: ByteArray,
        methodName: String,
        inject: (MethodVisitor, String) -> Unit
    ): (List<String>) -> ByteArray {
        return { injectNames ->
            val reader = ClassReader(origin)
            val writer = ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
            reader.accept(object : ClassVisitor(Opcodes.ASM9, writer) {
                override fun visitMethod(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    exceptions: Array<out String>?
                ): MethodVisitor {
                    val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
                    if (name != methodName || descriptor != "()V") {
                        return mv
                    }
                    return object : MethodVisitor(Opcodes.ASM9, mv) {
                        override fun visitInsn(opcode: Int) {
                            if (opcode == Opcodes.RETURN) {
                                injectNames.forEach { className ->
                                    inject(mv, className)
                                }
                            }
                            super.visitInsn(opcode)
                        }
                    }
                }
            }, 0)
            writer.toByteArray()
        }
    }

    private class PutCollector(
        private val out: MutableList<ServicePut>
    ) : MethodVisitor(Opcodes.ASM9) {
        private val args = mutableListOf<Any?>()

        override fun visitLdcInsn(value: Any?) {
            args.add(value)
        }

        override fun visitInsn(opcode: Int) {
            when (opcode) {
                Opcodes.ICONST_0 -> args.add(false)
                Opcodes.ICONST_1 -> args.add(true)
                Opcodes.ICONST_2 -> args.add(2)
                Opcodes.ICONST_3 -> args.add(3)
                Opcodes.ICONST_4 -> args.add(4)
                Opcodes.ICONST_5 -> args.add(5)
                Opcodes.ICONST_M1 -> args.add(-1)
            }
        }

        override fun visitIntInsn(opcode: Int, operand: Int) {
            if (opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH) {
                args.add(operand)
            }
        }

        override fun visitMethodInsn(
            opcode: Int,
            owner: String,
            name: String,
            descriptor: String,
            isInterface: Boolean
        ) {
            if (name == ScanSetting.SERVICE_PUT_NAME && owner == ScanSetting.SERVICE_LOADER_OWNER) {
                val types = args.filterIsInstance<Type>()
                val strings = args.filterIsInstance<String>()
                val bools = args.filterIsInstance<Boolean>()
                if (types.size >= 2) {
                    out.add(
                        ServicePut(
                            interfaceName = types[0].className,
                            implName = types[1].className,
                            key = strings.getOrNull(0).orEmpty(),
                            defaultImpl = bools.getOrNull(1) ?: false
                        )
                    )
                }
            }
            args.clear()
        }
    }

    private data class ServicePut(
        val interfaceName: String,
        val implName: String,
        val key: String,
        val defaultImpl: Boolean
    )
}
