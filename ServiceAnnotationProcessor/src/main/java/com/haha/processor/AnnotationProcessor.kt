package com.haha.processor

import com.google.auto.service.AutoService
import com.haha.service.annotation.BindView
import com.haha.service.annotation.OnClick
import java.io.IOException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements

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
class AnnotationProcessor : AbstractProcessor() {
    private val TAG = javaClass.simpleName

    private var filerUtils: Filer? = null
    private var elementUtils: Elements? = null
    private var messager: Messager? = null
    private var options: Map<String, String>? = null
    private var helper: ProcessorHelper? = null

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)

        filerUtils = processingEnv?.filer
        elementUtils = processingEnv?.elementUtils
        messager = processingEnv?.messager
        options = processingEnv?.options
        helper = ProcessorHelper()
        helper!!.setMessager(messager)
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

    private fun handleBindViewProcess(roundEnv: RoundEnvironment?) {
        roundEnv ?: return
        val elements = roundEnv.getElementsAnnotatedWith(BindView::class.java)
        elements.forEach {
            if (it.kind == ElementKind.FIELD) {
                if (it is VariableElement) {
                    val typeElement = it.enclosingElement as? TypeElement
                    val packageElement = elementUtils!!.getPackageOf(typeElement)
                    val key = packageElement.simpleName.toString() + typeElement?.simpleName.toString()
                    val processorBean = helper!!.getOrEmpty(key)
                    processorBean.apply {
                        addVariableElement(it)
                        setTypeElement(typeElement)
                        fileName = typeElement?.simpleName.toString() + MConstants._VIEW_BINDING
                        packageName = packageElement.qualifiedName.toString()
                        targetName = typeElement?.simpleName.toString()
                    }
                }
            } else if (it.kind == ElementKind.CLASS) {
                if (it is TypeElement) {
                    val typeElement = it
                    val packageElement = elementUtils!!.getPackageOf(typeElement)
                    val key = packageElement.simpleName.toString() + it.simpleName.toString()
                    val processorBean = helper!!.getOrEmpty(key)
                    processorBean.apply {
                        setTypeElement(it)
                        fileName = typeElement.simpleName.toString() + MConstants._VIEW_BINDING
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
                    val packageElement = elementUtils?.getPackageOf(typeElement) ?: return@forEach
                    val key = packageElement.simpleName.toString() + typeElement?.simpleName.toString()
                    val processorBean = helper?.getOrEmpty(key)
                    processorBean?.apply {
                        addMethodElement(it)
                        setTypeElement(typeElement)
                        fileName = typeElement?.simpleName.toString() + MConstants._VIEW_BINDING
                        packageName = packageElement.qualifiedName.toString()
                        targetName = typeElement?.simpleName.toString()
                    }
                }
            }
        }
    }

    private fun createJavaFiles() {
        try {
            helper?.createFiles(filerUtils)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}