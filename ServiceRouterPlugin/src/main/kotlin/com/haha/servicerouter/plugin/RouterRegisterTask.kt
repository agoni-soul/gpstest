package com.haha.servicerouter.plugin

import org.gradle.api.DefaultTask
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
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * 扫描全部 CLASSES，收集 Loader，改写 Router.loadRouterMap。
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
        val written = hashSetOf<String>()

        collectFromInputs(routeLoaders, interceptorLoaders)
        logger.lifecycle("[DOFRouter] Auto-register routes: $routeLoaders")
        logger.lifecycle("[DOFRouter] Auto-register interceptors: $interceptorLoaders")

        val injectNames = ArrayList<String>(routeLoaders.size + interceptorLoaders.size).apply {
            addAll(routeLoaders)
            addAll(interceptorLoaders)
        }

        JarOutputStream(BufferedOutputStream(FileOutputStream(output.get().asFile))).use { jos ->
            allJars.get().forEach { file ->
                copyJar(file.asFile, jos, written, injectNames)
            }
            allDirectories.get().forEach { dir ->
                copyDirectory(dir.asFile, jos, written, injectNames)
            }
        }
    }

    private fun collectFromInputs(
        routeLoaders: MutableSet<String>,
        interceptorLoaders: MutableSet<String>
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
                            interceptorLoaders
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
                        interceptorLoaders
                    )
                }
        }
    }

    private fun classifyLoader(
        entryName: String,
        input: InputStream,
        routeLoaders: MutableSet<String>,
        interceptorLoaders: MutableSet<String>
    ) {
        if (!entryName.startsWith(ScanSetting.ROUTES_PACKAGE) || !entryName.endsWith(".class")) {
            input.close()
            return
        }
        input.use { stream ->
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
            }, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
            val name = className ?: return
            val dotted = name.replace('/', '.')
            when {
                interfaces.contains(ScanSetting.IROUTE_LOADER) -> routeLoaders.add(dotted)
                interfaces.contains(ScanSetting.IINTERCEPTOR_LOADER) -> interceptorLoaders.add(
                    dotted
                )
            }
        }
    }

    private fun copyJar(
        jarFile: File,
        jos: JarOutputStream,
        written: MutableSet<String>,
        injectNames: List<String>
    ) {
        JarFile(jarFile).use { jar ->
            jar.entries().asSequence().forEach { entry ->
                if (entry.isDirectory || !written.add(entry.name)) {
                    return@forEach
                }
                jar.getInputStream(entry).use { input ->
                    val bytes = if (entry.name == "${ScanSetting.GENERATE_TO_CLASS_FILE}") {
                        injectRegister(input.readBytes(), injectNames)
                    } else {
                        input.readBytes()
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
        injectNames: List<String>
    ) {
        dir.walkTopDown().filter { it.isFile }.forEach { file ->
            val relative = file.relativeTo(dir).invariantSeparatorsPath
            if (!written.add(relative)) {
                return@forEach
            }
            val bytes = if (relative == ScanSetting.GENERATE_TO_CLASS_FILE) {
                injectRegister(file.readBytes(), injectNames)
            } else {
                file.readBytes()
            }
            writeEntry(jos, relative, bytes)
        }
    }

    private fun writeEntry(jos: JarOutputStream, name: String, bytes: ByteArray) {
        jos.putNextEntry(JarEntry(name))
        jos.write(bytes)
        jos.closeEntry()
    }

    private fun injectRegister(origin: ByteArray, injectNames: List<String>): ByteArray {
        if (injectNames.isEmpty()) {
            logger.lifecycle("[DOFRouter] no loader found, keep original loadRouterMap")
            return origin
        }
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
                if (name != ScanSetting.GENERATE_TO_METHOD || descriptor != "()V") {
                    return mv
                }
                return object : MethodVisitor(Opcodes.ASM9, mv) {
                    override fun visitInsn(opcode: Int) {
                        if (opcode == Opcodes.RETURN) {
                            injectNames.forEach { className ->
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
                        }
                        super.visitInsn(opcode)
                    }
                }
            }
        }, 0)
        logger.lifecycle("[DOFRouter] injected ${injectNames.size} register() into Router.loadRouterMap")
        return writer.toByteArray()
    }
}
