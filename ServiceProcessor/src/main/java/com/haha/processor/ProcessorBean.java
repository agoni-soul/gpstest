package com.haha.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

public class ProcessorBean {

    private String fileName;
    private String packageName;
    private String targetName;
    private MethodSpec mInjectMethod;
    private TypeSpec mClass;
    private JavaFile mFile;
    private ParameterSpec parameter;
    private Element element;
    VariableElement variableElement;
    ExecutableElement executableElement;

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


    public MethodSpec getInjectMethod() {
        return mInjectMethod;
    }

    public void setInjectMethod(MethodSpec mInjectMethod) {
        this.mInjectMethod = mInjectMethod;
    }

    public TypeSpec getClazz() {
        return mClass;
    }

    public void setClazz(TypeSpec mClass) {
        this.mClass = mClass;
    }

    public JavaFile getFile() {
        return mFile;
    }

    public void setFile(JavaFile mFile) {
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

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }
}
