package com.haha.servicerouterprocess.apt;

import static javax.lang.model.element.Modifier.PUBLIC;

import com.google.auto.service.AutoService;
import com.haha.servicerouterannotation.annotation.Interceptor;
import com.haha.servicerouterannotation.annotation.data.InterceptorMetaData;
import com.haha.servicerouterannotation.annotation.utils.Consts;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * @auther: haha
 * @Date: 2026/1/3
 * @Detail: 扫描 @Interceptor，按模块生成 IInterceptorLoader 实现
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.haha.servicerouterannotation.annotation.Interceptor")
public class InterceptorProcessor extends BaseProcessor {

    private final TreeMap<Integer, InterceptorMetaData> interceptorMap = new TreeMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations == null || annotations.isEmpty()) {
            Logger.info(">>> InterceptorProcessor annotations is empty. <<<");
            return false;
        }

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Interceptor.class);
        try {
            Logger.info(">>> Found interceptor, start... <<<");
            parseInterceptors(elements);
        } catch (Exception e) {
            Logger.error(e);
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(Collections.singletonList(Interceptor.class.getCanonicalName()));
    }

    private void parseInterceptors(Set<? extends Element> elements) throws IOException {
        if (elements == null || elements.isEmpty()) {
            Logger.info(">>> No @Interceptor found. <<<");
            return;
        }
        Logger.info(">>> Found interceptors, size is " + elements.size() + " <<<");

        interceptorMap.clear();

        TypeMirror tmInterceptor = typeMirrorOf(Consts.INTERCEPTOR);

        ParameterizedTypeName mapTypeOfInterceptorLoader = ParameterizedTypeName.get(
                ClassName.get(TreeMap.class),
                ClassName.get(Integer.class),
                ClassName.get(InterceptorMetaData.class)
        );
        ParameterSpec mapParamSpec = ParameterSpec.builder(mapTypeOfInterceptorLoader, "map").build();

        MethodSpec.Builder interceptorLoaderFunBuilder = MethodSpec.methodBuilder(Consts.METHOD_LOAD)
                .addParameter(mapParamSpec)
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC);

        for (Element element : elements) {
            Interceptor interceptorAnn = element.getAnnotation(Interceptor.class);
            if (interceptorAnn == null) {
                continue;
            }
            boolean isInterceptor = tmInterceptor == null
                    || mTypes.isSubtype(element.asType(), tmInterceptor);
            if (!isInterceptor) {
                Logger.warn("Interceptor " + element.getSimpleName() + " does not impl IRouteInterceptor");
                continue;
            }
            if (interceptorMap.containsKey(interceptorAnn.priority())) {
                Logger.warn("Interceptor priority " + interceptorAnn.priority() + " already exists, skip "
                        + element.getSimpleName());
                continue;
            }

            interceptorMap.put(
                    interceptorAnn.priority(),
                    new InterceptorMetaData(interceptorAnn.priority(), element.asType().toString(), Object.class)
            );
            interceptorLoaderFunBuilder.addStatement(
                    "map.put($L, new $T($L, $S, $T.class))",
                    interceptorAnn.priority(),
                    InterceptorMetaData.class,
                    interceptorAnn.priority(),
                    interceptorAnn.name().trim(),
                    element.asType()
            );
        }

        if (interceptorMap.isEmpty()) {
            return;
        }

        String fileName = Consts.INTERCEPTOR_LOADER_NAME + "_" + moduleName;
        TypeSpec typeIInterceptorLoader = TypeSpec.classBuilder(fileName)
                .addSuperinterface(ClassName.bestGuess(Consts.IINTERCEPTOR_LOADER))
                .addModifiers(PUBLIC)
                .addMethod(interceptorLoaderFunBuilder.build())
                .build();
        JavaFile.builder(Consts.PACKAGE, typeIInterceptorLoader)
                .build()
                .writeTo(mFiler);
        Logger.info(">>> Generate " + Consts.PACKAGE + "." + fileName + " <<<");
    }

    private TypeMirror typeMirrorOf(String className) {
        TypeElement element = mElements.getTypeElement(className);
        return element == null ? null : element.asType();
    }
}
