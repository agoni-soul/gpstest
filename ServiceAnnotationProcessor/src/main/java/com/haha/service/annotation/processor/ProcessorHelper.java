package com.haha.processor;

import com.haha.service.annotation.BindView;
import com.haha.service.annotation.OnClick;
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


    public void createFiles(Filer filer) {
        for (ProcessorBean processor : builderMaps.values()) {
            if (processor == null) return;
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
        if (processor.getFile() != null || processor.getTypeSpec() == null) return;
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
        CodeBlock.Builder builder = CodeBlock.builder();
        buildBindViewClassJavaCode(builder, processor);
        buildBindViewFieldJavaCode(builder, processor);
        buildOnClickMethodJavaCode(builder, processor);
        return builder.build();
    }

    /**
     *  activity.setContentView( 2131427358 );
     */
    //setContentView方法生成
    private void buildBindViewClassJavaCode(CodeBlock.Builder builder, ProcessorBean processor) {
        if (processor == null) {
            return;
        }
        TypeElement typeElement = processor.getTypeElement();
        if (typeElement != null) {
            BindView bindView = typeElement.getAnnotation(BindView.class);
            if (bindView != null) {
                int annotationValue = bindView.value();
                builder.add(
                        "if ($L > 0) {\n $L.setContentView( $L );\n}\n",
                        annotationValue, processor.getTargetName().toLowerCase(), annotationValue
                );
            }
        }
    }

    /**
     * activity.textView1 = activity.findViewById( 2131231131 );
     */
    //findViewById方法生成
    private void buildBindViewFieldJavaCode(CodeBlock.Builder builder, ProcessorBean processor) {
        if (processor == null) {
            return;
        }
        String targetName = processor.getTargetName().toLowerCase();
        for (VariableElement element : processor.getVariableElements()) {
            BindView bindView = element.getAnnotation(BindView.class);
            if (bindView == null) continue;
            int annotationValue = bindView.value();
            TypeName viewType = ClassName.bestGuess(element.asType().toString());
            builder.add(targetName + "." + element.getSimpleName() + " = ($T) $L.findViewById( $L );\n", viewType, targetName, annotationValue);
        }
    }

    /**
     * activity.button.setOnClickListener(new android.view.View OnClickListener() {
     *       @Override
     *       public void onClick(android.view.View v) {
     *         activity.onItbirdClick(v);
     *       }
     *     });
     */
    //setOnClickListener方法生成
    private void buildOnClickMethodJavaCode(CodeBlock.Builder builder, ProcessorBean processor) {
        if (processor == null) {
            return;
        }

        for (ExecutableElement element : processor.getMethodElements()) {
            if (!ElementKind.METHOD.equals(element.getKind())) continue;
            OnClick onClick = element.getAnnotation(OnClick.class);
            if (onClick == null) continue;
            for (int id : onClick.value()) {
//                for (VariableElement variableElement : processor.getVariableElements()) {
//                    BindView bindView = variableElement.getAnnotation(BindView.class);
//                    if (bindView.value() == id) {
//                    }
//                }
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
                        element.getSimpleName()
                );
            }
        }
    }
}
