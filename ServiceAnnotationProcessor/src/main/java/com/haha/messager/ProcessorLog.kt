package com.haha.messager

import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic
import javax.lang.model.element.*

/**
 *
 * @author:     haha
 * @date:       2024/9/11
 * Description: 打印AnnotationProcessor相关的日志
 *
 **/

class ProcessorLog : Messager {
    private val TAG = javaClass.simpleName
    override fun printMessage(kind: Diagnostic.Kind, msg: CharSequence) {
        println("$TAG: $kind - $msg")
    }

    override fun printMessage(kind: Diagnostic.Kind, msg: CharSequence, e: Element) {
        println("$TAG: $kind - $msg - $e")
    }

    override fun printMessage(
        kind: Diagnostic.Kind?,
        msg: CharSequence?,
        e: Element?,
        a: AnnotationMirror?,
        v: AnnotationValue?
    ) {
        println("$TAG: $kind - $msg - $e - $a - $v")
    }

    override fun printMessage(
        kind: Diagnostic.Kind?,
        msg: CharSequence?,
        e: Element?,
        a: AnnotationMirror?
    ) {
        println("$TAG: $kind - $msg - $e - $a")
    }
}
