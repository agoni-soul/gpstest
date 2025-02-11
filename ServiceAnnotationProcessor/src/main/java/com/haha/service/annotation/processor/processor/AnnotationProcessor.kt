package com.haha.service.annotation.processor.processor

import com.google.auto.service.AutoService
import com.haha.service.annotation.BindView
import com.haha.service.annotation.OnClick
import java.io.IOException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

/**
 *
 * @author : haha
 * @date   : 2024-09-09
 * @desc   : 学习并测试[AbstractProcessor]
 * @version: 1.0
 *
 */
//自动生成META-INF/services/javax.annotation.processing.Processor文件，使javac可以发现当前自定义注解处理器
@AutoService(Processor::class)
class AnnotationProcessor : BaseProcessor() {

    private var helper: ProcessorHelper? = null

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        helper = ProcessorHelper()
        helper!!.setMessager(mMessager)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        val types = LinkedHashSet<String>()
        types.add(getBindViewClass().canonicalName)
        types.add(getOnClickClass().canonicalName)
        return types
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        handleBindViewProcess(roundEnv)
        handleOnClickProcess(roundEnv)
        createJavaFiles()
        return false
    }

    private fun getBindViewClass(): Class<out Annotation> {
        return BindView::class.java
    }

    /**
     * @Target:注解的作用目标
     * @Target(ElementType.TYPE)——接口、类、枚举、注解
     * @Target(ElementType.FIELD)——字段、枚举的常量
     * @Target(ElementType.METHOD)——方法
     * @Target(ElementType.PARAMETER)——方法参数
     * @Target(ElementType.CONSTRUCTOR) ——构造函数
     * @Target(ElementType.LOCAL_VARIABLE)——局部变量
     * @Target(ElementType.ANNOTATION_TYPE)——注解
     * @Target(ElementType.PACKAGE)——包
     * @Retention：注解的保留位置
     * RetentionPolicy.SOURCE:这种类型的Annotations只在源代码级别保留,编译时就会被忽略,在class字节码文件中不包含。
     * RetentionPolicy.CLASS:这种类型的Annotations编译时被保留,默认的保留策略,在class文件中存在,但JVM将会忽略,运行时无法获得。
     * RetentionPolicy.RUNTIME:这种类型的Annotations将被JVM保留,所以他们能在运行时被JVM或其他使用反射机制的代码所读取和使用。
     * @Document：说明该注解将被包含在javadoc中
     * @Inherited：说明子类可以继承父类中的该注解
     */
    private fun handleBindViewProcess(roundEnv: RoundEnvironment?) {
        roundEnv ?: return
        val elements = roundEnv.getElementsAnnotatedWith(BindView::class.java)
        elements.forEach {
            if (it.kind == ElementKind.FIELD) {
                if (it is VariableElement) {
                    val typeElement = it.enclosingElement as? TypeElement
                    val packageElement = mElementUtils!!.getPackageOf(typeElement)
                    val key = packageElement.simpleName.toString() + typeElement?.simpleName.toString()
                    val processorBean = helper!!.getOrEmpty(key)
                    processorBean.apply {
                        addVariableElement(it)
                        setTypeElement(typeElement)
                        fileName = typeElement?.simpleName.toString() + ConstantUtils._VIEW_BINDING
                        packageName = packageElement.qualifiedName.toString()
                        targetName = typeElement?.simpleName.toString()
                    }
                }
            } else if (it.kind == ElementKind.CLASS) {
                if (it is TypeElement) {
                    val typeElement = it
                    val packageElement = mElementUtils!!.getPackageOf(typeElement)
                    val key = packageElement.simpleName.toString() + it.simpleName.toString()
                    val processorBean = helper!!.getOrEmpty(key)
                    processorBean.apply {
                        setTypeElement(it)
                        fileName = typeElement.simpleName.toString() + ConstantUtils._VIEW_BINDING
                        packageName = packageElement.qualifiedName.toString()
                        targetName = typeElement.simpleName.toString()
                    }
                }
            }
        }
    }

    private fun getOnClickClass(): Class<out Annotation> {
        return OnClick::class.java
    }

    private fun handleOnClickProcess(roundEnv: RoundEnvironment?) {
        roundEnv ?: return
        val elements = roundEnv.getElementsAnnotatedWith(OnClick::class.java)
        elements.forEach {
            if (it.kind == ElementKind.METHOD) {
                if (it is ExecutableElement) {
                    val typeElement: TypeElement? = it.enclosingElement as? TypeElement
                    val packageElement = mElementUtils?.getPackageOf(typeElement) ?: return@forEach
                    val key = packageElement.simpleName.toString() + typeElement?.simpleName.toString()
                    val processorBean = helper?.getOrEmpty(key)
                    processorBean?.apply {
                        addMethodElement(it)
                        setTypeElement(typeElement)
                        fileName = typeElement?.simpleName.toString() + ConstantUtils._VIEW_BINDING
                        packageName = packageElement.qualifiedName.toString()
                        targetName = typeElement?.simpleName.toString()
                    }
                }
            }
        }
    }

    private fun createJavaFiles() {
        try {
            helper?.createFiles(mFiler)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}