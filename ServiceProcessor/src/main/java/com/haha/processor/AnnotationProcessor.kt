package com.haha.processor

import com.google.auto.service.AutoService
import com.haha.annotation.BindView
import com.haha.annotation.OnClick
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
import javax.tools.Diagnostic

/**
 *
 * @author : haha
 * @date   : 2024-09-09
 * @desc   : 学习并测试[AbstractProcessor]
 * @version: 1.0
 *
 */
@AutoService(Processor::class)
class AnnotationProcessor: AbstractProcessor() {
    private val TAG = javaClass.simpleName

    private var filerUtils: Filer? = null
    private var elementUtils: Elements? = null
    private var messager: Messager? = null
    private var options: Map<String, String>? = null
    private var helper: ProcessorHelper? = null

    private val map = HashMap<String, ProcessorBean>()

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)

        filerUtils = processingEnv?.filer
        elementUtils = processingEnv?.elementUtils
        messager = processingEnv?.messager
        options = processingEnv?.options
        helper = ProcessorHelper()
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
        handleBindViewProcess(annotations, roundEnv)
//        handleOnClickProcess(annotations, roundEnv)
        createJavaFiles()
        return roundEnv?.processingOver() ?: false
    }

    private fun getBindViewClass(): Class<out Annotation> {
        return BindView::class.java
    }

    private fun handleBindViewProcess(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ) {
        roundEnv ?: return
        annotations ?: return
        val elements = roundEnv.getElementsAnnotatedWith(BindView::class.java)
        elements.forEach {
            if (it.kind == ElementKind.FIELD) {
                val enclosingElement = it.enclosingElement
                val packageElement = elementUtils?.getPackageOf(enclosingElement) ?: return@forEach
                val key = enclosingElement.simpleName.toString()
                println("handleBindViewProcess: $key")
                val processorBean: ProcessorBean?
                if (map.containsKey(key)) {
                    processorBean = map[key]
                } else {
                    processorBean = ProcessorBean()
                    map[key] = processorBean
                }
                processorBean?.apply {
                    if (it is VariableElement) {
                        addVariableElement(it)
                        if (it.enclosingElement is TypeElement) {
                            typeElement = it.enclosingElement as TypeElement
                        }
                    }
                    fileName = enclosingElement.simpleName.toString() + MConstants._VIEW_BINDING
                    packageName = packageElement.qualifiedName.toString()
                    targetName = enclosingElement.simpleName.toString()
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
        helper?.createFiles(map, processingEnv, elementUtils)
    }
}