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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
@SupportedAnnotationTypes({"com.midea.base.core.dofrouter.annotation.Route"})
public class RouteProcessor extends BaseProcessor {

    private HashMap<String, RouteMetaData> routeMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        Logger.info(">>> RouteProcessor init. <<<");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!annotations.isEmpty()) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Route.class);

            try {
                Logger.info(">>> Found routes, start... <<<");
                this.parseRoutes(elements);

            } catch (Exception e) {
                Logger.error(e);
            }

            return true;
        }

        Logger.info(">>> annotations is empty. <<<");
        return false;
    }

    private void parseRoutes(Set<? extends Element> elements) throws IOException {
        if (!elements.isEmpty()) {
            Logger.info(">>> Found routes, size is " + elements.size() + " <<<");

            routeMap.clear();

            TypeMirror tmActivity = mElements.getTypeElement(RouteType.ACTIVITY.getClassName()).asType();
            TypeMirror tmFragment = mElements.getTypeElement(RouteType.FRAGMENT.getClassName()).asType();
            TypeMirror tmFragmentX = mElements.getTypeElement(RouteType.FRAGMENT_X.getClassName()).asType();

            ParameterizedTypeName mapTypeOfRouteLoader = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), ClassName.get(RouteMetaData.class));
            ParameterSpec mapParamSpec = ParameterSpec.builder(mapTypeOfRouteLoader, "map").build();

            //Generate implement IRouteLoader interface class
            MethodSpec.Builder routeLoaderFunSpecBuild = MethodSpec.methodBuilder(Consts.METHOD_LOAD)
                    .addParameter(mapParamSpec)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC);

            if (!elements.isEmpty()) {
                for (Element element : elements) {
                    Route routeAnn = element.getAnnotation(Route.class);

                    RouteType routeType;
                    if (mTypes.isSubtype(element.asType(), tmActivity)) {
                        Logger.info("Found Activity " + element.asType());
                        routeType = RouteType.ACTIVITY;
                    } else if (mTypes.isSubtype(element.asType(), tmFragment)) {
                        Logger.info("Found Fragment " + element.asType());
                        routeType = RouteType.FRAGMENT;
                    } else if (mTypes.isSubtype(element.asType(), tmFragmentX)) {
                        Logger.info("Found Fragment_androidx " + element.asType());
                        routeType = RouteType.FRAGMENT_X;
                    } else {
                        Logger.info("Unknown route " + element.asType());
                        routeType = RouteType.UNKNOWN;
                    }

                    if (routeAnn.path().length() > 0) {
                        if (routeMap.containsKey(routeAnn.path())) {
                            Logger.warn("The route ${routeMap[routeAnn.path]?.name} already has Path { ${routeAnn.path} }, so skip route ${it.asType()}");
                            continue;
                        }
                        routeMap.put(routeAnn.path(), new RouteMetaData(routeType, routeAnn.priority(), routeAnn.name(), routeAnn.path(), routeAnn.pathPrefix(), routeAnn.pathPattern(), Object.class));

                        routeLoaderFunSpecBuild.addStatement(
                                "map.put($S, new $T($T.$L, $L, $S, $S, $S, $S, $T.class))",
                                routeAnn.path(),
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
                }
            }

            String fileName = Consts.ROUTE_LOADER_NAME + "_" + moduleName;
            TypeSpec typeIRouteLoader = TypeSpec.classBuilder(fileName)
                    .addSuperinterface(ClassName.get(mElements.getTypeElement(Consts.PACKAGE + ".api.interfaces.IRouteLoader")))
                    .addModifiers(PUBLIC)
                    .addMethod(routeLoaderFunSpecBuild.build())
                    .build();
            JavaFile.builder(Consts.PACKAGE, typeIRouteLoader)
                    .build()
                    .writeTo(mFiler);
        }
    }
}
