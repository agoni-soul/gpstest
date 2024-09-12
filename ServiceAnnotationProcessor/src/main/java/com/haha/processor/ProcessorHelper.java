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
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

public class ProcessorHelper {

    private final Map<String, ProcessorBean> builderMaps;
    private Messager messager;

    public ProcessorHelper() {
        this.builderMaps = new HashMap<>();
    }

    public void put(String key, ProcessorBean processor) {
        builderMaps.put(key, processor);
    }

    public void setMessager(Messager messager) {
        this.messager = messager;
    }

    public ProcessorBean getOrEmpty(String key) {
        if (builderMaps.get(key) == null) put(key, new ProcessorBean());
        return builderMaps.get(key);
    }


    public void createFiles(Filer filer) throws IOException {
        for (ProcessorBean processor : builderMaps.values()) {
            checkAndBuildParameter(processor);
            checkAndBuildInject(processor);
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
//                        .returns(void.class)
                        .addParameter(processor.getParameter())
//                        .addAnnotation(MConstants.CLASSNAME_UI_THREAD)
                        .addCode(buildJavaCode(processor))
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

    private CodeBlock buildJavaCode(ProcessorBean processor) {
        Element element = processor.getElement();
        CodeBlock.Builder builder = CodeBlock.builder();
        if (element == null) return builder.build();
        ElementKind kind = element.getKind();
        if (kind == null) return builder.build();

        switch (kind) {
            case CLASS: {
                break;
            }
            case FIELD: {
                builder.add(buildBindViewFieldJavaCode(processor));
                break;
            }
            case METHOD: {
                builder.add(buildOnClickMethodJavaCode(processor));
                break;
            }
            default: {

            }
        }
        return builder.build();
    }

    private CodeBlock buildBindViewFieldJavaCode(ProcessorBean processor) {
        CodeBlock.Builder builder = CodeBlock.builder();
        if (processor == null) return builder.build();
        Element element = processor.getElement();
        if (element == null) return builder.build();
        BindView bindView = element.getAnnotation(BindView.class);
        if (bindView == null) return builder.build();

        String elementStr = element.getSimpleName().toString();
        builder.add(
                "$L.$L=$L.findViewById($L);\n",
                processor.getTargetName().toLowerCase(),
                elementStr,
                processor.getTargetName().toLowerCase(),
                bindView.value()
        );
        messager.printMessage(Diagnostic.Kind.ERROR, "buildBindViewFieldJavaCode: " + builder);

        return builder.build();
    }

    private CodeBlock buildOnClickMethodJavaCode(ProcessorBean processor) {
        CodeBlock.Builder builder = CodeBlock.builder();
        if (processor == null) return builder.build();
        Element element = processor.getElement();
        if (element == null) return builder.build();
        OnClick onClick = element.getAnnotation(OnClick.class);
        if (onClick == null) return builder.build();

        String elementStr = element.getSimpleName().toString();
        for (int id : onClick.value()) {
            builder.add(
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
                    );
        }
        return builder.build();
    }
}
