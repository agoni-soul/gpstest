package com.haha.processor;

import com.haha.service.annotation.BindView;
import com.haha.service.annotation.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

public class ProcessorHelper {

    private Map<String, ProcessorBean> builderMaps;

    public ProcessorHelper() {
        this.builderMaps = new HashMap<>();
    }


    public void put(String key, ProcessorBean processor) {
        builderMaps.put(key, processor);
    }


    public ProcessorBean getOrEmpty(String key) {
        if (builderMaps.get(key) == null) put(key, new ProcessorBean());
        return builderMaps.get(key);
    }


    public void createFiles(Filer filer) throws IOException {
        for (ProcessorBean processor : builderMaps.values()) {
            checkAndBuildParameter(processor);
            checkAndBuildInject(processor);
            buildJavaCode(processor);
            checkAndBuildClass(processor);
            checkAndBuildFile(processor);
            if (processor.getFile() != null) {
                processor.getFile().writeTo(filer);
            }
        }
    }

    public void checkAndBuildFile(ProcessorBean processor) {
        if (processor.getFile() != null) return;
        JavaFile javaFile =
                JavaFile.builder(processor.getPackageName(), processor.getTypeSpec())
                        .build();
        processor.setFile(javaFile);
    }

    public void checkAndBuildClass(ProcessorBean processor) {
        if (processor.getTypeSpec() != null) return;
        TypeSpec typeSpec =
                TypeSpec.classBuilder(processor.getFileName())
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(processor.getMethodSpec())
                        .build();
        processor.setTypeSpec(typeSpec);
    }

    public void checkAndBuildInject(ProcessorBean processor) {
        if (processor.getMethodSpec() != null) return;
        MethodSpec methodSpec =
                MethodSpec.methodBuilder(MConstants.INJECT_NAME)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(processor.getParameter())
//                        .addAnnotation(MConstants.CLASSNAME_UI_THREAD)
                        .build();
        processor.setMethodSpec(methodSpec);
    }

    public void checkAndBuildParameter(ProcessorBean processor) {
        if (processor.getParameter() != null) return;
        ClassName targetClass = ClassName.get(processor.getPackageName(), processor.getTargetName());
        ParameterSpec parameterSpec =
                ParameterSpec.builder(targetClass, processor.getTargetName().toLowerCase())
                        .addModifiers(Modifier.FINAL)
                        .build();
        processor.setParameter(parameterSpec);
    }

    public void buildJavaCode(ProcessorBean processor) {
        Element element = processor.getElement();
        if (element == null) {
            return;
        }
        ElementKind kind = element.getKind();
        if (kind == null) {
            return;
        }
        switch (kind) {
            case CLASS: {
                break;
            }
            case FIELD: {
                buildBindViewJavaCode(processor);
                break;
            }
            case METHOD: {
                buildOnClickJavaCode(processor);
                break;
            }
            default: {

            }
        }
    }

    private void buildBindViewJavaCode(ProcessorBean processor) {
        if (processor == null) return;
        Element element = processor.getElement();
        if (element == null) return;
        BindView bindView = element.getAnnotation(BindView.class);
        String elementStr = element.getSimpleName().toString();
        if (bindView == null) return;
        MethodSpec methodSpec = processor.getMethodSpec();
        if (methodSpec == null) return;

        processor.setMethodSpec(
                methodSpec.toBuilder()
                        .addStatement(
                                "$L.$L=$L.findViewById($L)",
                                processor.getTargetName().toLowerCase(),
                                elementStr,
                                processor.getTargetName().toLowerCase(),
                                bindView.value()
                        )
                        .build()
        );
    }

    private void buildOnClickJavaCode(ProcessorBean processor) {
        if (processor == null) return;
        Element element = processor.getElement();
        if (element == null) return;
        OnClick onClick = element.getAnnotation(OnClick.class);
        String elementStr = element.getSimpleName().toString();
        if (onClick == null) return;
        MethodSpec methodSpec = processor.getMethodSpec();
        if (methodSpec == null) return;

        for (int id : onClick.value()) {
            methodSpec = methodSpec.toBuilder()
                    .addStatement(
                            "$L.findViewById($L).setOnClickListener(new $T.OnClickListener() {\n" +
                                    "      @Override\n" +
                                    "      public void onClick(View v) {\n" +
                                    "        $L.$L(v);\n" +
                                    "      }\n" +
                                    "    });\n",
                            processor.getTargetName().toLowerCase(),
                            id,
                            MConstants.CLASSNAME_VIEW,
                            processor.getTargetName().toLowerCase(),
                            elementStr
                    )
                    .build();
        }
        processor.setMethodSpec(methodSpec);
    }
}
