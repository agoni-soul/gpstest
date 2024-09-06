package com.haha.processor;

import com.google.auto.service.AutoService;
import com.haha.annotation.BindView;
import com.haha.annotation.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class MPrecessor extends AbstractProcessor {
    private Filer filerUtils; //
    private Elements elementUtils; //
    private Messager messagerUtils; //
    private Map<String, String> options; //
    private ProcessorHelper helper;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        filerUtils = processingEnvironment.getFiler();
        elementUtils = processingEnvironment.getElementUtils();
        messagerUtils = processingEnvironment.getMessager();
        options = processingEnvironment.getOptions();

        helper = new ProcessorHelper();
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(BindView.class.getCanonicalName());
        types.add(OnClick.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("process");

        processBindView(roundEnvironment);
        processOnClick(roundEnvironment);

        try {
            helper.createFiles(filerUtils);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void processBindView(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(BindView.class)) {
//            if (element.getKind() == ElementKind.FIELD) {
                Element enclosingElement = element.getEnclosingElement();
                PackageElement packageOf = elementUtils.getPackageOf(enclosingElement);
                String key = packageOf.toString() + enclosingElement.getSimpleName().toString();
                System.out.println("processBindView: " + key);

                BindView annotation = element.getAnnotation(BindView.class);
                ProcessorBean processor = helper.getOrEmpty(key);
                processor.setFileName(enclosingElement.getSimpleName().toString() + "_ViewBinding");
                processor.setPackageName(packageOf.toString());
                processor.setTargetName(enclosingElement.getSimpleName().toString());
                helper.checkAndBuildParameter(processor);
                helper.checkAndBuildInject(processor);
                processor.setmInjectMethod(processor
                        .getmInjectMethod()
                        .toBuilder()
                        .addStatement("$L.$L=$L.findViewById($L)",
                                enclosingElement.getSimpleName().toString().toLowerCase(),
                                element.getSimpleName().toString(),
                                enclosingElement.getSimpleName().toString().toLowerCase(),
                                annotation.value())
                        .build());
                System.out.println("processBindView: " + enclosingElement.getSimpleName().toString().toLowerCase());
                System.out.println("processBindView: " + element.getSimpleName().toString());
                System.out.println("processBindView: " + enclosingElement.getSimpleName().toString().toLowerCase());
                System.out.println("processBindView: " + annotation.value());

//            } else {
//                logE("bindview", "@BindView not support this kind %s", element.getKind());
//            }
        }
    }

    private void processOnClick(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(OnClick.class)) {
            if (element.getKind() == ElementKind.METHOD) {

                Element enclosingElement = element.getEnclosingElement();
                PackageElement packageOf = elementUtils.getPackageOf(enclosingElement);
                String key = packageOf.toString() + enclosingElement.getSimpleName().toString();

                OnClick annotation = element.getAnnotation(OnClick.class);
                ProcessorBean processor = helper.getOrEmpty(key);
                processor.setFileName(enclosingElement.getSimpleName().toString() + "_ViewBinding");
                processor.setPackageName(packageOf.toString());
                processor.setTargetName(enclosingElement.getSimpleName().toString());
                helper.checkAndBuildParameter(processor);
                helper.checkAndBuildInject(processor);
                MethodSpec methodSpec = processor.getmInjectMethod();
                for (int id : annotation.value()) {
                    methodSpec = methodSpec.toBuilder().addStatement("$L.findViewById($L).setOnClickListener(new $T.OnClickListener() {\n" +
                                            "      @Override\n" +
                                            "      public void onClick(View v) {\n" +
                                            "        $L.$L(v);\n" +
                                            "      }\n" +
                                            "    })\n",
                                    enclosingElement.getSimpleName().toString().toLowerCase(),
                                    id,
                                    MConstants.CLASSNAME_VIEW,
                                    enclosingElement.getSimpleName().toString().toLowerCase(),
                                    element.getSimpleName().toString()
                            )
                            .build();
                }
                processor.setmInjectMethod(methodSpec);

            } else {
                logE("OnClick", "@OnClick not support this kind %s", element.getKind());
            }
        }
    }


    private void logE(String tag, String format, Object... objs) {
        messagerUtils.printMessage(Diagnostic.Kind.ERROR, String.format(format, objs));
    }
}
