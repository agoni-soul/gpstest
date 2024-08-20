package com.soul.bleSDK.constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * author : yangzy33
 * time   : 2024-08-20
 * desc   :
 * version: 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.SOURCE)
public @interface ScanSettings {
    int value() default 0;
}
