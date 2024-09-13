package com.haha.processor

import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

/**
 *
 * @author:     haha
 * @date:       2024/9/11
 * Description: AnnotationProcessorBean
 *
 **/
class ProcessorBean {
    var fileName: String = ""
    var packageName: String = ""
    var targetName: String = ""
    var typeElement: TypeElement? = null
        private set
    var methodSpec: MethodSpec? = null
    var typeSpec: TypeSpec? = null
    var file: JavaFile? = null
    var parameter: ParameterSpec? = null
    val variableElements: MutableList<VariableElement> by lazy {
        mutableListOf()
    }
    val methodElements: MutableList<ExecutableElement> by lazy {
        mutableListOf()
    }

    fun setTypeElement(element: TypeElement?) {
        if (this.typeElement == null && element != null) {
            this.typeElement = element
        }
    }

    fun addVariableElement(variableElement: VariableElement?) {
        variableElement?.let {
            variableElements.add(it)
        }
    }

    fun addMethodElement(executableElement: ExecutableElement?) {
        executableElement?.let {
            methodElements.add(it)
        }
    }
}
