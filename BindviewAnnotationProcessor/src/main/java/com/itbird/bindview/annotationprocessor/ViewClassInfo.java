package com.itbird.bindview.annotationprocessor;

import com.itbird.bindview.annotation.ItbirdAopBinderView;
import com.itbird.bindview.annotation.ItbirdOnclick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;


/**
 * 解析注解，创建相关view信息类
 * Created by itbird on 2022/4/15
 */
public class ViewClassInfo {

    private static final int INVAILD = -1;

    public static final String BIND_METHOD_PARAMETER_NAME = "activity";
    /**
     * 类元素
     */
    private TypeElement typeElement;
    /**
     * 类的view属性集合
     */
    private List<VariableElement> variableElements;
    /**
     * 类的method集合
     */
    private List<ExecutableElement> methodElements;

    public String getClassFullName() {
        return typeElement != null ? typeElement.getQualifiedName().toString() : null;
    }

    public String getClassSimpleName() {
        return typeElement != null ? typeElement.getSimpleName().toString() : null;
    }

    public int getClassLayoutId() {
        if (typeElement == null) {
            return INVAILD;
        }

        //获取类注解
        ItbirdAopBinderView annotaion = typeElement.getAnnotation(ItbirdAopBinderView.class);

        if (annotaion == null) {
            return INVAILD;
        }
        //获取注解的value
        return annotaion.value();
    }

    public void addMethodElement(ExecutableElement element) {
        if (methodElements == null) {
            methodElements = new ArrayList<>();
        }
        methodElements.add(element);
    }

    public void addVariableElement(VariableElement element) {
        if (variableElements == null) {
            variableElements = new ArrayList<>();
        }
        variableElements.add(element);
    }

    public void setTypeElement(TypeElement typeElement) {
        if (this.typeElement == null) {
            this.typeElement = typeElement;
        }
    }

    /**
     * 创建java代码
     *
     * @return
     */
    private CodeBlock generateJavaCode() {
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        /**
         *  activity.setContentView( 2131427358 );
         */
        //setContentView方法生成
        if (typeElement != null) {
            codeBlock.add(BIND_METHOD_PARAMETER_NAME + ".setContentView( $L );\n", getItbirdAopBinderViewAnnotationValue(typeElement));
        }

        /**
         * activity.textView1 = activity.findViewById( 2131231131 );
         */
        //findViewById方法生成
        if (variableElements != null) {
            for (VariableElement element : variableElements) {
                codeBlock.add(BIND_METHOD_PARAMETER_NAME + "." + element.getSimpleName() + " = " + BIND_METHOD_PARAMETER_NAME + ".findViewById( $L );\n", getItbirdAopBinderViewAnnotationValue(element));
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
        if (methodElements != null) {
            for (ExecutableElement element : methodElements) {
                int[] ids = element.getAnnotation(ItbirdOnclick.class).value();
                for (int id : ids) {
                    for (VariableElement variableElement : variableElements) {
                        if (getItbirdAopBinderViewAnnotationValue(variableElement) == id) {
                            //TODO 这儿暂时都是以直接写死的字符串，来直接生成的代码，第二版本，可以考虑，通过注解优化适配
                            //TODO FOR循环优化
                            codeBlock.add(BIND_METHOD_PARAMETER_NAME + "." + variableElement.getSimpleName() + ".setOnClickListener(new android.view.View.OnClickListener() {\n"
                                    + "@Override\n"
                                    + "public void onClick(android.view.View v) {\n"
                                    + BIND_METHOD_PARAMETER_NAME + "." + element.getSimpleName() + "(v);\n"
                                    + "}\n"
                                    + " });\n");
                            break;
                        }
                    }
                }
            }
        }
        return codeBlock.build();
    }

    private int getItbirdAopBinderViewAnnotationValue(Element element) {
        ItbirdAopBinderView annotaion = element.getAnnotation(ItbirdAopBinderView.class);
        //TODO :这儿应该不会为空，为什么会空指针
        return annotaion == null ? INVAILD : annotaion.value();
    }

    public void createJavaCode(ProcessingEnvironment mProcessingEnvironment, Elements elementUtils) {
        //获取类信息
        ClassName className = ClassName.get(typeElement);
        //构建bind的入参
        ParameterSpec parameterSpec = ParameterSpec.builder(className, BIND_METHOD_PARAMETER_NAME).build();
        MethodSpec methodSpec = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .addParameter(parameterSpec)
                .addCode(generateJavaCode())
                .build();

        //构造类
        TypeSpec typeSpec = TypeSpec.classBuilder(getClassSimpleName() + "_ViewBinding")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodSpec)
                .build();
        //创建文件
        JavaFile javaFile = JavaFile.builder(getPackageName(elementUtils), typeSpec).
                build();

        //写入文件
        try {
            javaFile.writeTo(mProcessingEnvironment.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPackageName(Elements elementUtils) {
        if (typeElement == null) {
            return null;
        }
        PackageElement packageElement = elementUtils.getPackageOf(typeElement);
        return packageElement.getQualifiedName().toString();
    }
}
