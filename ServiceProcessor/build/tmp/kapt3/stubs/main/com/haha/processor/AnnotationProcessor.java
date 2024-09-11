package com.haha.processor;

import java.lang.System;

/**
 * @author : haha
 * @date   : 2024-09-09
 * @desc   : 学习并测试[AbstractProcessor]
 * @version: 1.0
 */
@com.google.auto.service.AutoService(value = {javax.annotation.processing.Processor.class})
@kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000n\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u001b\n\u0002\b\u0002\n\u0002\u0010#\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0012\u001a\u00020\u0013H\u0002J\u0010\u0010\u0014\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00160\u0015H\u0002J\u0010\u0010\u0017\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00160\u0015H\u0002J\u000e\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00040\u0019H\u0016J\b\u0010\u001a\u001a\u00020\u001bH\u0016J\u0012\u0010\u001c\u001a\u00020\u00132\b\u0010\u001d\u001a\u0004\u0018\u00010\u001eH\u0002J$\u0010\u001f\u001a\u00020\u00132\u0010\u0010 \u001a\f\u0012\u0006\b\u0001\u0012\u00020!\u0018\u00010\u00192\b\u0010\u001d\u001a\u0004\u0018\u00010\u001eH\u0002J\u0012\u0010\"\u001a\u00020\u00132\b\u0010#\u001a\u0004\u0018\u00010\rH\u0016J$\u0010$\u001a\u00020%2\u0010\u0010 \u001a\f\u0012\u0006\b\u0001\u0012\u00020!\u0018\u00010\u00192\b\u0010\u001d\u001a\u0004\u0018\u00010\u001eH\u0016R\u0016\u0010\u0003\u001a\n \u0005*\u0004\u0018\u00010\u00040\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0010\u001a\u0010\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006&"}, d2 = {"Lcom/haha/processor/AnnotationProcessor;", "Ljavax/annotation/processing/AbstractProcessor;", "()V", "TAG", "", "kotlin.jvm.PlatformType", "elementUtils", "Ljavax/lang/model/util/Elements;", "filerUtils", "Ljavax/annotation/processing/Filer;", "helper", "Lcom/haha/processor/ProcessorHelper;", "mProcessingEnvironment", "Ljavax/annotation/processing/ProcessingEnvironment;", "messager", "Ljavax/annotation/processing/Messager;", "options", "", "createJavaFiles", "", "getBindViewClass", "Ljava/lang/Class;", "", "getOnClickClass", "getSupportedAnnotationTypes", "", "getSupportedSourceVersion", "Ljavax/lang/model/SourceVersion;", "handleBindViewProcess", "roundEnv", "Ljavax/annotation/processing/RoundEnvironment;", "handleOnClickProcess", "annotations", "Ljavax/lang/model/element/TypeElement;", "init", "processingEnv", "process", "", "ServiceProcessor"})
@javax.annotation.processing.SupportedSourceVersion(value = javax.lang.model.SourceVersion.RELEASE_8)
@javax.annotation.processing.SupportedAnnotationTypes(value = {"kim.hsl.router_annotation.Route"})
public final class AnnotationProcessor extends javax.annotation.processing.AbstractProcessor {
    private final java.lang.String TAG = null;
    private javax.annotation.processing.ProcessingEnvironment mProcessingEnvironment;
    private javax.annotation.processing.Filer filerUtils;
    private javax.lang.model.util.Elements elementUtils;
    private javax.annotation.processing.Messager messager;
    private java.util.Map<java.lang.String, java.lang.String> options;
    private com.haha.processor.ProcessorHelper helper;
    
    public AnnotationProcessor() {
        super();
    }
    
    @java.lang.Override()
    public void init(@org.jetbrains.annotations.Nullable()
    javax.annotation.processing.ProcessingEnvironment processingEnv) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.util.Set<java.lang.String> getSupportedAnnotationTypes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public javax.lang.model.SourceVersion getSupportedSourceVersion() {
        return null;
    }
    
    @java.lang.Override()
    public boolean process(@org.jetbrains.annotations.Nullable()
    java.util.Set<? extends javax.lang.model.element.TypeElement> annotations, @org.jetbrains.annotations.Nullable()
    javax.annotation.processing.RoundEnvironment roundEnv) {
        return false;
    }
    
    private final java.lang.Class<? extends java.lang.annotation.Annotation> getBindViewClass() {
        return null;
    }
    
    private final void handleBindViewProcess(javax.annotation.processing.RoundEnvironment roundEnv) {
    }
    
    private final java.lang.Class<? extends java.lang.annotation.Annotation> getOnClickClass() {
        return null;
    }
    
    private final void handleOnClickProcess(java.util.Set<? extends javax.lang.model.element.TypeElement> annotations, javax.annotation.processing.RoundEnvironment roundEnv) {
    }
    
    private final void createJavaFiles() {
    }
}