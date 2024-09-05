//package com.haha.processor;
//
//import com.squareup.javapoet.ClassName;
//import com.squareup.javapoet.JavaFile;
//import com.squareup.javapoet.MethodSpec;
//import com.squareup.javapoet.ParameterSpec;
//import com.squareup.javapoet.TypeSpec;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.annotation.processing.Filer;
//import javax.lang.model.element.Modifier;
//
//public class ProcessorHelper {
//
//    private Map<String, ProcessorBean> builderMaps;
//
//    public ProcessorHelper() {
//        this.builderMaps = new HashMap<>();
//    }
//
//
//    public void put(String key, ProcessorBean processor) {
//        builderMaps.put(key, processor);
//    }
//
//
//    public ProcessorBean getOrEmpty(String key) {
//        if (builderMaps.get(key) == null) put(key, new ProcessorBean());
//        return builderMaps.get(key);
//    }
//
//
//    public void createFiles(Filer filer) throws IOException {
//        for (ProcessorBean processor : builderMaps.values()) {
//            checkAndBuildParameter(processor);
//            checkAndBuildInject(processor);
//            checkAndBuildClass(processor);
//            checkAndBuildFile(processor);
//            if (processor.getmFile()!=null) {
//                processor.getmFile().writeTo(filer);
//            }
//        }
//    }
//
//    public void checkAndBuildFile(ProcessorBean processor){
//        if (processor.getmFile()!=null) return;
//        processor.setmFile(JavaFile.builder(processor.getPackageName(),processor.getmClass()).build());
//    }
//
//    public void checkAndBuildClass(ProcessorBean processor){
//        if (processor.getmClass()!=null)return;
//        processor.setmClass(TypeSpec.classBuilder(processor.getFileName())
//                .addModifiers(Modifier.PUBLIC)
//                .addMethod(processor.getmInjectMethod())
//                .build());
//    }
//
//    public void checkAndBuildInject(ProcessorBean processor){
//        if (processor.getmInjectMethod()!=null)return;
//        processor.setmInjectMethod(MethodSpec.methodBuilder(MConstants.INJECT_NAME)
//                .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
//                .returns(void.class)
//                .addParameter(processor.getParameter())
//                .addAnnotation(MConstants.CLASSNAME_UI_THREAD)
//                .build());
//    }
//
//
//    public void checkAndBuildParameter(ProcessorBean processor){
//        if (processor.getParameter()!=null)return;
//        ClassName targetClass=ClassName.get(processor.getPackageName(),processor.getTargetName());
//        processor.setParameter(ParameterSpec.builder(targetClass,processor.getTargetName().toLowerCase())
//                .addModifiers(Modifier.FINAL)
//                .build());
//    }
//
//
//
//}
