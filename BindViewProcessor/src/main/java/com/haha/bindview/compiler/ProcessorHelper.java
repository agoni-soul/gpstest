package com.haha.bindview.compiler;

import com.haha.bindview.annotation.BindView;
import com.haha.bindview.annotation.BindViewConsts;
import com.haha.bindview.annotation.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class ProcessorHelper {

    private static final ClassName CLASSNAME_VIEW = ClassName.get("android.view", "View");

    private final Map<String, ProcessorBean> builderMaps = new HashMap<>();

    public void put(String key, ProcessorBean processor) {
        builderMaps.put(key, processor);
    }

    public void setMessager(Messager messager) {
    }

    public ProcessorBean getOrEmpty(String key) {
        if (builderMaps.get(key) == null) {
            put(key, new ProcessorBean());
        }
        return builderMaps.get(key);
    }

    public void createFiles(Filer filer) {
        for (ProcessorBean processor : builderMaps.values()) {
            if (processor == null) {
                return;
            }
            checkAndBuildParameter(processor);
            checkAndBuildInject(processor);
            checkAndBuildClass(processor);
            checkAndBuildFile(processor);
            if (processor.getFile() != null) {
                try {
                    processor.getFile().writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void checkAndBuildFile(ProcessorBean processor) {
        if (processor.getFile() != null || processor.getTypeSpec() == null) {
            return;
        }
        processor.setFile(JavaFile.builder(processor.getPackageName(), processor.getTypeSpec()).build());
    }

    public void checkAndBuildClass(ProcessorBean processor) {
        if (processor.getTypeSpec() != null) {
            return;
        }
        processor.setTypeSpec(
                TypeSpec.classBuilder(processor.getFileName())
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(processor.getMethodSpec())
                        .build()
        );
    }

    public void checkAndBuildInject(ProcessorBean processor) {
        if (processor.getMethodSpec() != null) {
            return;
        }
        processor.setMethodSpec(
                MethodSpec.methodBuilder(BindViewConsts.INJECT_NAME)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(processor.getParameter())
                        .addCode(buildJavaCode(processor))
                        .build()
        );
    }

    public void checkAndBuildParameter(ProcessorBean processor) {
        if (processor.getParameter() != null) {
            return;
        }
        ClassName targetClass = ClassName.get(processor.getPackageName(), processor.getTargetName());
        processor.setParameter(
                ParameterSpec.builder(targetClass, processor.getTargetName().toLowerCase())
                        .addModifiers(Modifier.FINAL)
                        .build()
        );
    }

    private CodeBlock buildJavaCode(ProcessorBean processor) {
        CodeBlock.Builder builder = CodeBlock.builder();
        buildBindViewClassJavaCode(builder, processor);
        buildBindViewFieldJavaCode(builder, processor);
        buildOnClickMethodJavaCode(builder, processor);
        return builder.build();
    }

    private void buildBindViewClassJavaCode(CodeBlock.Builder builder, ProcessorBean processor) {
        TypeElement typeElement = processor.getTypeElement();
        if (typeElement == null) {
            return;
        }
        BindView bindView = typeElement.getAnnotation(BindView.class);
        if (bindView != null) {
            int annotationValue = bindView.value();
            builder.add(
                    "if ($L > 0) {\n $L.setContentView( $L );\n}\n",
                    annotationValue, processor.getTargetName().toLowerCase(), annotationValue
            );
        }
    }

    private void buildBindViewFieldJavaCode(CodeBlock.Builder builder, ProcessorBean processor) {
        String targetName = processor.getTargetName().toLowerCase();
        for (VariableElement element : processor.getVariableElements()) {
            BindView bindView = element.getAnnotation(BindView.class);
            if (bindView == null) {
                continue;
            }
            TypeName viewType = ClassName.bestGuess(element.asType().toString());
            builder.add(
                    targetName + "." + element.getSimpleName() + " = ($T) $L.findViewById( $L );\n",
                    viewType, targetName, bindView.value()
            );
        }
    }

    private void buildOnClickMethodJavaCode(CodeBlock.Builder builder, ProcessorBean processor) {
        for (ExecutableElement element : processor.getMethodElements()) {
            if (!ElementKind.METHOD.equals(element.getKind())) {
                continue;
            }
            OnClick onClick = element.getAnnotation(OnClick.class);
            if (onClick == null) {
                continue;
            }
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
                        CLASSNAME_VIEW,
                        processor.getTargetName().toLowerCase(),
                        element.getSimpleName()
                );
            }
        }
    }
}
