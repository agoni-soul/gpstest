package com.haha.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

public class ProcessorBean {

    private String fileName;
    private String packageName;
    private String targetName;
    private MethodSpec mInjectMethod;
    private TypeSpec mClass;
    private JavaFile mFile;
    private ParameterSpec parameter;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


    public MethodSpec getmInjectMethod() {
        return mInjectMethod;
    }

    public void setmInjectMethod(MethodSpec mInjectMethod) {
        this.mInjectMethod = mInjectMethod;
    }

    public TypeSpec getmClass() {
        return mClass;
    }

    public void setmClass(TypeSpec mClass) {
        this.mClass = mClass;
    }

    public JavaFile getmFile() {
        return mFile;
    }

    public void setmFile(JavaFile mFile) {
        this.mFile = mFile;
    }

    public ParameterSpec getParameter() {
        return parameter;
    }

    public void setParameter(ParameterSpec parameter) {
        this.parameter = parameter;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
}
