package com.haha.processor

import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

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
    var element: Element? = null
    var methodSpec: MethodSpec? = null
    var typeSpec: TypeSpec? = null
    var file: JavaFile? = null
    var parameter: ParameterSpec? = null
}
