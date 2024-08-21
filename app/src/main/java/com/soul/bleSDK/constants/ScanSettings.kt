package com.soul.bleSDK.constants


/**
 *     author : yangzy33
 *     time   : 2024-08-20
 *     desc   :
 *     version: 1.0
 */

enum class ScanSettings(val callbackType: Int) {
    /**
     * 默认值
     */
    CALLBACK_TYPE_DEFAULT(0),

    /**
     * Trigger a callback for every Bluetooth advertisement found that matches the filter criteria.
     * If no filter is active, all advertisement packets are reported.
     */
    CALLBACK_TYPE_ALL_MATCHES(1),

    /**
     * A result callback is only triggered for the first advertisement packet
     * received that matches the filter criteria.
     */
    CALLBACK_TYPE_FIRST_MATCH(2),

    /**
     * Receive a callback when advertisements are no longer received from a device
     * that has been previously reported by a first match callback.
     */
    CALLBACK_TYPE_MATCH_LOST(3),

    /**
     * 移除已绑定的设备
     */
    CALLBACK_TYPE_REMOVE_BOUND_DEVICE(100);
}

fun Int.toScanSettings(): ScanSettings {
    return when (this) {
        ScanSettings.CALLBACK_TYPE_DEFAULT.callbackType -> {
            ScanSettings.CALLBACK_TYPE_DEFAULT
        }
        ScanSettings.CALLBACK_TYPE_ALL_MATCHES.callbackType -> {
            ScanSettings.CALLBACK_TYPE_ALL_MATCHES
        }
        ScanSettings.CALLBACK_TYPE_FIRST_MATCH.callbackType -> {
            ScanSettings.CALLBACK_TYPE_FIRST_MATCH
        }
        ScanSettings.CALLBACK_TYPE_MATCH_LOST.callbackType -> {
            ScanSettings.CALLBACK_TYPE_MATCH_LOST
        }
        ScanSettings.CALLBACK_TYPE_REMOVE_BOUND_DEVICE.callbackType -> {
            ScanSettings.CALLBACK_TYPE_REMOVE_BOUND_DEVICE
        }
        else -> {
            ScanSettings.CALLBACK_TYPE_DEFAULT
        }
    }
}