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
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * @auther: haha
 * @Date: 2026/1/3
 * @Detail:
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.midea.base.core.dofrouter.annotation.Interceptor"})
public class InterceptorProcessor extends BaseProcessor {

    private TreeMap<Integer, InterceptorMetaData> interceptorMap = new TreeMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        Logger.debug(">>> InterceptorProcessor init. <<<");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!annotations.isEmpty()) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Interceptor.class);

            try {
                Logger.info(">>> Found interceptor, start... <<<");
                this.parseInterceptors(elements);

            } catch (Exception e) {
                Logger.error(e);
            }

            return true;
        }

        Logger.info(">>> annotations is empty. <<<");
        return false;
    }

    private void parseInterceptors(Set<? extends Element> elements) throws IOException {
        if (!elements.isEmpty()) {
            Logger.info(">>> Found interceptors, size is " + elements.size() + " <<<");

            interceptorMap.clear();

            TypeMirror tmInterceptor = mElements.getTypeElement(Consts.INTERCEPTOR).asType();

            ParameterizedTypeName mapTypeOfInterceptorLoader = ParameterizedTypeName.get(ClassName.get(TreeMap.class), ClassName.get(Integer.class), ClassName.get(InterceptorMetaData.class));
            ParameterSpec mapParamSpec = ParameterSpec.builder(mapTypeOfInterceptorLoader, "map").build();

            //Generate implement IRouteLoader interface class
            MethodSpec.Builder interceptorLoaderFunBuilder = MethodSpec.methodBuilder(Consts.METHOD_LOAD)
                    .addParameter(mapParamSpec)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC);

            if (!elements.isEmpty()) {
                for (Element element : elements) {
                    Interceptor interceptorAnn = element.getAnnotation(Interceptor.class);
                    if (mTypes.isSubtype(element.asType(), tmInterceptor)) {
                        if (interceptorMap.containsKey(interceptorAnn.priority())) {
                            continue;
                        }

                        interceptorMap.put(interceptorAnn.priority(), new InterceptorMetaData(interceptorAnn.priority(), element.asType().toString(), Object.class));
                        interceptorLoaderFunBuilder.addStatement(
                                "map.put(" + interceptorAnn.priority() + ", new $T($L, $S, $T.class))",
                                InterceptorMetaData.class,
                                interceptorAnn.priority(),
                                interceptorAnn.name().trim(),
                                element.asType());
                    } else {
                        Logger.warn("Interceptor " + element.getSimpleName() + "does not impl IInterceptor");
                    }
                }
            }

            String fileName = Consts.INTERCEPTOR_LOADER_NAME + "_" + moduleName;
            TypeSpec typeIInterceptorLoader = TypeSpec.classBuilder(fileName)
                    .addSuperinterface(ClassName.get(mElements.getTypeElement(Consts.PACKAGE + ".api.interfaces.IInterceptorLoader")))
                    .addModifiers(PUBLIC)
                    .addMethod(interceptorLoaderFunBuilder.build())
                    .build();
            JavaFile.builder(Consts.PACKAGE, typeIInterceptorLoader)
                    .build()
                    .writeTo(mFiler);
        }
    }
}