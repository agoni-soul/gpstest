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
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
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
@SupportedAnnotationTypes("kim.hsl.router_annotation.Route")
//自动生成META-INF/services/javax.annotation.processing.Processor文件，使javac可以发现当前自定义注解处理器
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class AnnotationProcessor: AbstractProcessor() {
    private val TAG = javaClass.simpleName

    private var mProcessingEnvironment: ProcessingEnvironment? = null
    private var filerUtils: Filer? = null
    private var elementUtils: Elements? = null
    private var messager: Messager? = null
    private var options: Map<String, String>? = null
    private var helper: ProcessorHelper? = null

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)

        mProcessingEnvironment = processingEnv
        filerUtils = processingEnv?.filer
        elementUtils = processingEnv?.elementUtils
        messager = processingEnv?.messager
        options = processingEnv?.options
        helper = ProcessorHelper()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        val types = LinkedHashSet<String>()
//        types.add(getBindViewClass().canonicalName)
        types.add(BindView::class.java.canonicalName)
//        types.add(getOnClickClass().canonicalName)
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
//        handleOnClickProcess(annotations, roundEnv)
        try {
            helper?.createFiles(filerUtils);
        } catch (e: IOException) {
            e.printStackTrace();
        }
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
                val enclosingElement = it.enclosingElement
                val key = enclosingElement.simpleName.toString()
                println("handleBindViewProcess: $key")
                if (it is VariableElement) {
                    val variableElement = it
                    if (it.enclosingElement is TypeElement) {
                        val typeElement = it.enclosingElement as TypeElement
                        val packageElement = elementUtils!!.getPackageOf(typeElement)
                        val processorBean = helper!!.getOrEmpty(key)
                        processorBean.apply {
                            addVariableElement(variableElement)
                            fileName = typeElement.simpleName.toString() + MConstants._VIEW_BINDING
                            packageName = packageElement.qualifiedName.toString()
                            targetName = typeElement.simpleName.toString()
                        }
                    }
                }
            } else if (it.kind == ElementKind.CLASS) {
                if (it is TypeElement) {
                    val typeElement = it
                    val packageElement = elementUtils!!.getPackageOf(typeElement)
                    val key = it.simpleName.toString()
                    val processorBean = helper!!.getOrEmpty(key)
                    processorBean.apply {
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

    private fun handleOnClickProcess(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ) {
        roundEnv ?: return
        annotations ?: return
        val elements = roundEnv.getElementsAnnotatedWith(OnClick::class.java)
        elements.forEach {
            val enclosingElement = it.enclosingElement
            val packageElement = elementUtils?.getPackageOf(enclosingElement) ?: return@forEach
//            val key = packageElement.toString() + enclosingElement.simpleName.toString()
            val key = enclosingElement.simpleName.toString()
            println("handleBindViewProcess: $key")
            val processorBean = helper?.getOrEmpty(key)
            processorBean?.apply {
//                typeElement = it
                executableElement = typeElement as ExecutableElement
                fileName = enclosingElement.simpleName.toString() + MConstants._VIEW_BINDING
                packageName = packageElement.qualifiedName.toString()
                targetName = enclosingElement.simpleName.toString()
            }
        }
    }

    private fun createJavaFiles() {
//        for (className in map.keys) {
//            val processorBean = map[className]
//            if (processorBean != null) {
//                helper?.createFiles(processorBean, processingEnv, elementUtils)
//            }
//        }
        helper?.createFiles(processingEnv, elementUtils)
    }
}