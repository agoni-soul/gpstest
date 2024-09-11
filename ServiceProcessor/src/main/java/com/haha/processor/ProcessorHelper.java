package com.haha.processor;

import com.haha.annotation.BindView;
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
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

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
            checkAndBuildClass(processor);
            checkAndBuildFile(processor);
            if (processor.getFile() != null) {
                processor.getFile().writeTo(filer);
            }
        }
    }

    public void createFiles(Map<String, ProcessorBean> map, ProcessingEnvironment processingEnvironment, Elements elementUtils) {
        for (String key : map.keySet()) {
            ProcessorBean processor = map.get(key);
            createFiles(processor, processingEnvironment, elementUtils);
        }
    }

    public void createFiles(ProcessorBean processor, ProcessingEnvironment processingEnvironment, Elements elementUtils) {
        TypeElement typeElement = processor.getTypeElement();
        //获取类信息
        ClassName className = ClassName.get(typeElement);
        //构建bind的入参
        ParameterSpec parameterSpec = ParameterSpec.builder(className, "activity").build();
        MethodSpec methodSpec = MethodSpec.methodBuilder(MConstants.INJECT_NAME)
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .addParameter(parameterSpec)
//                .addCode(generateJavaCode(processor))
                .build();

        String classSimpleName = null;
        String packageName = null;
        if (typeElement != null) {
            classSimpleName = typeElement.getSimpleName().toString();
            PackageElement packageElement = elementUtils.getPackageOf(typeElement);
            packageName = packageElement.getQualifiedName().toString();
        }

        //构造类
        TypeSpec typeSpec =
                TypeSpec.classBuilder(classSimpleName + "_ViewBinding")
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(methodSpec)
                        .build();

        //创建文件
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

        try {
            javaFile.writeTo(processingEnvironment.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("createFiles: \n" + e.getMessage());
        }
    }

    public void createFiles(ProcessingEnvironment processingEnvironment, Elements elementUtils) {
        for (String key : builderMaps.keySet()) {
            ProcessorBean processor = builderMaps.get(key);
            checkAndBuildParameter(processor);
            checkAndBuildInject(processor);
            checkAndBuildClass(processor);
            checkAndBuildFile(processor);
            if (processor.getFile() != null) {
                try {
                    processor.getFile().writeTo(processingEnvironment.getFiler());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("createFiles: \n" + e.getMessage());
                }
            }
        }
    }

    public void checkAndBuildFile(ProcessorBean processor) {
        if (processor.getFile() != null) return;
        JavaFile javaFile =
                JavaFile.builder(processor.getPackageName(), processor.getClazz())
                        .build();
        processor.setFile(javaFile);
    }

    public void checkAndBuildClass(ProcessorBean processor) {
//        if (processor.getClazz() != null) return;
        TypeSpec typeSpec =
                TypeSpec.classBuilder(processor.getFileName())
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(processor.getInjectMethod())
                        .build();
        processor.setClazz(typeSpec);
    }

    public void checkAndBuildInject(ProcessorBean processor) {
        if (processor.getInjectMethod() != null) return;
        MethodSpec methodSpec =
                MethodSpec.methodBuilder(MConstants.INJECT_NAME)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(processor.getParameter())
//                        .addAnnotation(MConstants.CLASSNAME_UI_THREAD)
                        .addCode(generateJavaCode(processor))
                        .build();
        processor.setInjectMethod(methodSpec);
    }

    /**
     * 创建java代码
     *
     * @return
     */
    private CodeBlock generateJavaCode(ProcessorBean processor) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        TypeElement typeElement = processor.getTypeElement();
        String targetName = processor.getTargetName().toLowerCase();
        /**
         *  activity.setContentView( 2131427358 );
         */
        //setContentView方法生成
        if (typeElement != null) {
            BindView bindViewAnnotation = typeElement.getAnnotation(BindView.class);
            int annotationValue = bindViewAnnotation == null ? 0 : bindViewAnnotation.value();
            codeBlock.add(
                    "if ($L > 0) {\n" + targetName + ".setContentView( $L );\n" + "}\n",
                    annotationValue, annotationValue
            );
        }

        /**
         * activity.textView1 = activity.findViewById( 2131231131 );
         */
        //findViewById方法生成
        if (!processor.getVariableElementList().isEmpty()) {
            for (VariableElement element : processor.getVariableElementList()) {
                BindView bindViewAnnotation = element.getAnnotation(BindView.class);
                int annotationValue = bindViewAnnotation == null ? 0 : bindViewAnnotation.value();
                codeBlock.add(targetName + "." + element.getSimpleName() + " = " + targetName + ".findViewById( $L );\n", annotationValue);
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
        //TODO 这儿有个问题，是通过遍历view属性，去找到view控件，从而通过字符串形式去设置的onclick事件，如果view没有使用注解，则得不到这个view，导致方法注册事件
//        if (element instanceof ExecutableElement) {
//            int[] ids = element.getAnnotation(OnClick.class).value();
//            for (int id : ids) {
//                for (VariableElement variableElement : variableElements) {
//                    if (getItbirdAopBinderViewAnnotationValue(variableElement) == id) {
//                        //TODO 这儿暂时都是以直接写死的字符串，来直接生成的代码，第二版本，可以考虑，通过注解优化适配
//                        //TODO FOR循环优化
//                        codeBlock.add(BIND_METHOD_PARAMETER_NAME + "." + variableElement.getSimpleName() + ".setOnClickListener(new android.view.View.OnClickListener() {\n"
//                                + "@Override\n"
//                                + "public void onClick(android.view.View v) {\n"
//                                + BIND_METHOD_PARAMETER_NAME + "." + element.getSimpleName() + "(v);\n"
//                                + "}\n"
//                                + " });\n");
//                        break;
//                    }
//                }
//            }
//        }
        return codeBlock.build();
    }

    public void checkAndBuildParameter(ProcessorBean processor) {
        if (processor.getParameter() != null) return;
        ClassName targetClass = ClassName.get(processor.getPackageName(), processor.getTargetName());
        ParameterSpec parameterSpec =
                ParameterSpec.builder(targetClass, processor.getTargetName().toLowerCase())
//                        .addModifiers(Modifier.FINAL)
                        .build();
        processor.setParameter(parameterSpec);
    }
}
