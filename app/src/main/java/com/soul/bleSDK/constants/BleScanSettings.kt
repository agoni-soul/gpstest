package com.soul.bleSDK.constants

import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy


/**
 *     author : yangzy33
 *     time   : 2024-08-20
 *     desc   :
 *     version: 1.0
 */

enum class BleScanSettings(callbackType: Int) {
    CALLBACK_TYPE_ALL_MATCHES(1),
    CALLBACK_TYPE_FIRST_MATCH(2),
    CALLBACK_TYPE_MATCH_LOST(3),
    CALLBACK_TYPE_REMOVE_BOUND_DEVICE(100);
    var mCallbackType: Int = 0

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE, ElementType.METHOD, ElementType.LOCAL_VARIABLE)
    @interface ScanSettings {

    }

    fun upgrade(callbackType: Int) {
        mCallbackType = callbackType
    }
}