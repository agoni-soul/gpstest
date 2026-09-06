package com.haha.bindview.compiler

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.util.Elements

/**
 * BindView / OnClick 自学 APT 基类，与 ServiceLoader Processor 完全独立。
 */
abstract class BindViewBaseProcessor : AbstractProcessor() {
    protected var mFiler: Filer? = null
    protected var mElementUtils: Elements? = null
    protected var mMessager: Messager? = null

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        mFiler = processingEnv?.filer
        mElementUtils = processingEnv?.elementUtils
        mMessager = processingEnv?.messager
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }
}
