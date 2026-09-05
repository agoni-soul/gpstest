package com.haha.servicerouterprocess.apt;

import static javax.lang.model.element.Modifier.PUBLIC;

import com.google.auto.service.AutoService;
import com.haha.servicerouterannotation.annotation.Route;
import com.haha.servicerouterannotation.annotation.RouteType;
import com.haha.servicerouterannotation.annotation.data.RouteMetaData;
import com.haha.servicerouterannotation.annotation.utils.Consts;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * @auther: haha
 * @Date: 2026/1/3
 * @Detail: 扫描 @Route，按模块生成 IRouteLoader 实现
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.haha.servicerouterannotation.annotation.Route")
public class RouteProcessor extends BaseProcessor {

    private final HashMap<String, RouteMetaData> routeMap = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations == null || annotations.isEmpty()) {
            Logger.info(">>> RouteProcessor annotations is empty. <<<");
            return false;
        }

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Route.class);
        try {
            Logger.info(">>> Found routes, start... <<<");
            parseRoutes(elements);
        } catch (Exception e) {
            Logger.error(e);
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(Collections.singletonList(Route.class.getCanonicalName()));
    }

    private void parseRoutes(Set<? extends Element> elements) throws IOException {
        if (elements == null || elements.isEmpty()) {
            Logger.info(">>> No @Route found. <<<");
            return;
        }
        Logger.info(">>> Found routes, size is " + elements.size() + " <<<");

        routeMap.clear();

        TypeMirror tmActivity = typeMirrorOf(RouteType.ACTIVITY.getClassName());
        TypeMirror tmFragment = typeMirrorOf(RouteType.FRAGMENT.getClassName());
        TypeMirror tmFragmentX = typeMirrorOf(RouteType.FRAGMENT_X.getClassName());

        ParameterizedTypeName mapTypeOfRouteLoader = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouteMetaData.class)
        );
        ParameterSpec mapParamSpec = ParameterSpec.builder(mapTypeOfRouteLoader, "map").build();

        MethodSpec.Builder routeLoaderFunSpecBuild = MethodSpec.methodBuilder(Consts.METHOD_LOAD)
                .addParameter(mapParamSpec)
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC);

        for (Element element : elements) {
            Route routeAnn = element.getAnnotation(Route.class);
            if (routeAnn == null) {
                continue;
            }

            RouteType routeType;
            if (tmActivity != null && mTypes.isSubtype(element.asType(), tmActivity)) {
                Logger.info("Found Activity " + element.asType());
                routeType = RouteType.ACTIVITY;
            } else if (tmFragment != null && mTypes.isSubtype(element.asType(), tmFragment)) {
                Logger.info("Found Fragment " + element.asType());
                routeType = RouteType.FRAGMENT;
            } else if (tmFragmentX != null && mTypes.isSubtype(element.asType(), tmFragmentX)) {
                Logger.info("Found Fragment_androidx " + element.asType());
                routeType = RouteType.FRAGMENT_X;
            } else {
                Logger.info("Unknown route " + element.asType());
                routeType = RouteType.UNKNOWN;
            }

            String routeKey = resolveRouteKey(routeAnn);
            if (routeKey.isEmpty()) {
                Logger.warn("Skip route " + element.asType() + " : path / pathPrefix / pathPattern are empty");
                continue;
            }
            if (routeMap.containsKey(routeKey)) {
                Logger.warn("The route already has key { " + routeKey + " }, so skip " + element.asType());
                continue;
            }
            routeMap.put(routeKey, new RouteMetaData(
                    routeType,
                    routeAnn.priority(),
                    routeAnn.name(),
                    routeAnn.path(),
                    routeAnn.pathPrefix(),
                    routeAnn.pathPattern(),
                    Object.class
            ));

            routeLoaderFunSpecBuild.addStatement(
                    "map.put($S, new $T($T.$L, $L, $S, $S, $S, $S, $T.class))",
                    routeKey,
                    RouteMetaData.class,
                    RouteType.class,
                    routeType,
                    routeAnn.priority(),
                    routeAnn.name(),
                    routeAnn.path(),
                    routeAnn.pathPrefix(),
                    routeAnn.pathPattern(),
                    element.asType()
            );
        }

        if (routeMap.isEmpty()) {
            return;
        }

        String fileName = Consts.ROUTE_LOADER_NAME + "_" + moduleName;
        TypeSpec typeIRouteLoader = TypeSpec.classBuilder(fileName)
                .addSuperinterface(ClassName.bestGuess(Consts.IROUTE_LOADER))
                .addModifiers(PUBLIC)
                .addMethod(routeLoaderFunSpecBuild.build())
                .build();
        JavaFile.builder(Consts.PACKAGE, typeIRouteLoader)
                .build()
                .writeTo(mFiler);
        Logger.info(">>> Generate " + Consts.PACKAGE + "." + fileName + " <<<");
    }

    private String resolveRouteKey(Route routeAnn) {
        if (routeAnn.path().length() > 0) {
            return routeAnn.path();
        }
        if (routeAnn.pathPrefix().length() > 0) {
            return routeAnn.pathPrefix();
        }
        return routeAnn.pathPattern();
    }

    private TypeMirror typeMirrorOf(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }
        TypeElement element = mElements.getTypeElement(className);
        return element == null ? null : element.asType();
    }
}
