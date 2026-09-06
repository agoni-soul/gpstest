package com.haha.bindview.compiler

import com.google.auto.service.AutoService
import com.haha.bindview.annotation.BindView
import com.haha.bindview.annotation.BindViewConsts
import com.haha.bindview.annotation.OnClick
import java.io.IOException
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

@AutoService(Processor::class)
class BindViewProcessor : BindViewBaseProcessor() {

    private var helper: ProcessorHelper? = null

    override fun init(processingEnv: javax.annotation.processing.ProcessingEnvironment?) {
        super.init(processingEnv)
        helper = ProcessorHelper()
        helper!!.setMessager(mMessager)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return linkedSetOf(BindView::class.java.canonicalName, OnClick::class.java.canonicalName)
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

    private fun handleBindViewProcess(roundEnv: RoundEnvironment?) {
        roundEnv ?: return
        for (element in roundEnv.getElementsAnnotatedWith(BindView::class.java)) {
            if (element.kind == ElementKind.FIELD && element is VariableElement) {
                val typeElement = element.enclosingElement as? TypeElement ?: continue
                bindType(typeElement).addVariableElement(element)
            } else if (element.kind == ElementKind.CLASS && element is TypeElement) {
                bindType(element)
            }
        }
    }

    private fun handleOnClickProcess(roundEnv: RoundEnvironment?) {
        roundEnv ?: return
        for (element in roundEnv.getElementsAnnotatedWith(OnClick::class.java)) {
            if (element.kind != ElementKind.METHOD || element !is ExecutableElement) {
                continue
            }
            val typeElement = element.enclosingElement as? TypeElement ?: continue
            bindType(typeElement).addMethodElement(element)
        }
    }

    private fun bindType(typeElement: TypeElement): ProcessorBean {
        val packageElement = mElementUtils!!.getPackageOf(typeElement)
        val key = packageElement.simpleName.toString() + typeElement.simpleName.toString()
        return helper!!.getOrEmpty(key).apply {
            setTypeElement(typeElement)
            fileName = typeElement.simpleName.toString() + BindViewConsts.VIEW_BINDING_SUFFIX
            packageName = packageElement.qualifiedName.toString()
            targetName = typeElement.simpleName.toString()
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
