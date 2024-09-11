package com.haha.api;

import java.lang.System;

/**
 * @author : haha
 * @date   : 2024-09-03
 * @desc   : [IServiceLoader]注解实现类
 * @version: 1.0
 */
@kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\f\u0018\u0000 \u00142\u00020\u0001:\u0001\u0014B#\b\u0016\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007B\'\b\u0016\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\f\u0010\b\u001a\b\u0012\u0002\b\u0003\u0018\u00010\t\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\nJ\b\u0010\u0013\u001a\u00020\u0003H\u0016R\"\u0010\u0004\u001a\u0004\u0018\u00010\u00032\b\u0010\u000b\u001a\u0004\u0018\u00010\u0003@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR*\u0010\b\u001a\b\u0012\u0002\b\u0003\u0018\u00010\t2\f\u0010\u000b\u001a\b\u0012\u0002\b\u0003\u0018\u00010\t@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\"\u0010\u0002\u001a\u0004\u0018\u00010\u00032\b\u0010\u000b\u001a\u0004\u0018\u00010\u0003@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\rR\u001e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\u0006@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006\u0015"}, d2 = {"Lcom/haha/api/ServiceImpl;", "", "key", "", "implementation", "singleton", "", "(Ljava/lang/String;Ljava/lang/String;Z)V", "implementationClazz", "Ljava/lang/Class;", "(Ljava/lang/String;Ljava/lang/Class;Z)V", "<set-?>", "getImplementation", "()Ljava/lang/String;", "getImplementationClazz", "()Ljava/lang/Class;", "getKey", "getSingleton", "()Z", "toString", "Companion", "BaseAnnotation"})
public final class ServiceImpl {
    @org.jetbrains.annotations.NotNull()
    public static final com.haha.api.ServiceImpl.Companion Companion = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SINGLETON = "singleton";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DEFAULT_SERVICE_IMPL_KEY = "default serviceImpl key";
    @org.jetbrains.annotations.Nullable()
    private java.lang.String key;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String implementation = "";
    @org.jetbrains.annotations.Nullable()
    private java.lang.Class<?> implementationClazz;
    private boolean singleton = false;
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getKey() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getImplementation() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Class<?> getImplementationClazz() {
        return null;
    }
    
    public final boolean getSingleton() {
        return false;
    }
    
    public ServiceImpl(@org.jetbrains.annotations.Nullable()
    java.lang.String key, @org.jetbrains.annotations.Nullable()
    java.lang.String implementation, boolean singleton) {
        super();
    }
    
    public ServiceImpl(@org.jetbrains.annotations.Nullable()
    java.lang.String key, @org.jetbrains.annotations.Nullable()
    java.lang.Class<?> implementationClazz, boolean singleton) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/haha/api/ServiceImpl$Companion;", "", "()V", "DEFAULT_SERVICE_IMPL_KEY", "", "SINGLETON", "BaseAnnotation"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}