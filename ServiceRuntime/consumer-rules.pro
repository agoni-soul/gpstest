-keep interface com.haha.service.impl.service.IServiceInit { *; }
-keep class * implements com.haha.service.impl.service.IServiceInit { *; }
-keep class com.haha.service.impl.generated.ServiceLoaderInit { *; }
-keep class com.haha.service.impl.service.ServiceLoader {
    public static void put(java.lang.Class, java.lang.String, java.lang.Class, boolean, boolean, int, java.lang.String);
    public static void lazyInit();
    public static void init(android.content.Context, boolean);
}
