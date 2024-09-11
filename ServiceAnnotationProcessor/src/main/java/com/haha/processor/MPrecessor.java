package com.haha.processor;

//@AutoService(Processor.class)
//public class MPrecessor extends AbstractProcessor {
//    private Filer filerUtils; //
//    private Elements elementUtils; //
//    private Messager messagerUtils; //
//    private Map<String, String> options; //
//    private ProcessorHelper helper;
//
//    @Override
//    public synchronized void init(ProcessingEnvironment processingEnvironment) {
//        super.init(processingEnvironment);
//
//        filerUtils = processingEnvironment.getFiler();
//        elementUtils = processingEnvironment.getElementUtils();
//        messagerUtils = processingEnvironment.getMessager();
//        options = processingEnvironment.getOptions();
//
//        helper = new ProcessorHelper();
//    }
//
//
//    @Override
//    public Set<String> getSupportedAnnotationTypes() {
//        Set<String> types = new LinkedHashSet<>();
//        types.add(BindView.class.getCanonicalName());
//        types.add(OnClick.class.getCanonicalName());
//        return types;
//    }
//
//    @Override
//    public SourceVersion getSupportedSourceVersion() {
//        return SourceVersion.latestSupported();
//    }
//
//
//    @Override
//    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
//        System.out.println("process");
//
//        processBindView(roundEnvironment);
//        processOnClick(roundEnvironment);
//
//        try {
//            helper.createFiles(filerUtils);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//
//    private void processBindView(RoundEnvironment roundEnvironment) {
//        for (Element element : roundEnvironment.getElementsAnnotatedWith(BindView.class)) {
////            if (element.getKind() == ElementKind.FIELD) {
//                Element enclosingElement = element.getEnclosingElement();
//                PackageElement packageOf = elementUtils.getPackageOf(enclosingElement);
//                String key = packageOf.toString() + enclosingElement.getSimpleName().toString();
//                System.out.println("processBindView: " + key);
//
//                BindView annotation = element.getAnnotation(BindView.class);
//                ProcessorBean processor = helper.getOrEmpty(key);
//                processor.setFileName(enclosingElement.getSimpleName().toString() + "_ViewBinding");
//                processor.setPackageName(packageOf.toString());
//                processor.setTargetName(enclosingElement.getSimpleName().toString());
//                helper.checkAndBuildParameter(processor);
//                helper.checkAndBuildInject(processor);
//                processor.setInjectMethod(processor
//                        .getInjectMethod()
//                        .toBuilder()
//                        .addStatement("$L.$L=$L.findViewById($L)",
//                                enclosingElement.getSimpleName().toString().toLowerCase(),
//                                element.getSimpleName().toString(),
//                                enclosingElement.getSimpleName().toString().toLowerCase(),
//                                annotation.value())
//                        .build());
//                System.out.println("processBindView: " + enclosingElement.getSimpleName().toString().toLowerCase());
//                System.out.println("processBindView: " + element.getSimpleName().toString());
//                System.out.println("processBindView: " + enclosingElement.getSimpleName().toString().toLowerCase());
//                System.out.println("processBindView: " + annotation.value());
//
////            } else {
////                logE("bindview", "@BindView not support this kind %s", element.getKind());
////            }
//        }
//    }
//
//    private void processOnClick(RoundEnvironment roundEnvironment) {
//        for (Element element : roundEnvironment.getElementsAnnotatedWith(OnClick.class)) {
//            if (element.getKind() == ElementKind.METHOD) {
//
//                Element enclosingElement = element.getEnclosingElement();
//                PackageElement packageOf = elementUtils.getPackageOf(enclosingElement);
//                String key = packageOf.toString() + enclosingElement.getSimpleName().toString();
//
//                OnClick annotation = element.getAnnotation(OnClick.class);
//                ProcessorBean processor = helper.getOrEmpty(key);
//                processor.setFileName(enclosingElement.getSimpleName().toString() + "_ViewBinding");
//                processor.setPackageName(packageOf.toString());
//                processor.setTargetName(enclosingElement.getSimpleName().toString());
//                helper.checkAndBuildParameter(processor);
//                helper.checkAndBuildInject(processor);
//                MethodSpec methodSpec = processor.getInjectMethod();
//                for (int id : annotation.value()) {
//                    methodSpec = methodSpec.toBuilder().addStatement("$L.findViewById($L).setOnClickListener(new $T.OnClickListener() {\n" +
//                                            "      @Override\n" +
//                                            "      public void onClick(View v) {\n" +
//                                            "        $L.$L(v);\n" +
//                                            "      }\n" +
//                                            "    })\n",
//                                    enclosingElement.getSimpleName().toString().toLowerCase(),
//                                    id,
//                                    MConstants.CLASSNAME_VIEW,
//                                    enclosingElement.getSimpleName().toString().toLowerCase(),
//                                    element.getSimpleName().toString()
//                            )
//                            .build();
//                }
//                processor.setInjectMethod(methodSpec);
//
//            } else {
//                logE("OnClick", "@OnClick not support this kind %s", element.getKind());
//            }
//        }
//    }
//
//
//    private void logE(String tag, String format, Object... objs) {
//        messagerUtils.printMessage(Diagnostic.Kind.ERROR, String.format(format, objs));
//    }
//}
