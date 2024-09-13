package com.soul.blesdk.constants


/**
 *     author : haha
 *     time   : 2024-08-20
 *     desc   :
 *     version: 1.0
 */

enum class BleScanSettings(val callbackType: Int) {
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
    CALLBACK_TYPE_REMOVE_BOUND_DEVICE(100),

    /**
     * 低版本扫描的设备信息
     */
    CALLBACK_TYPE_LE_SCAN_DEVICE(101);
}

fun Int.toScanSettings(): BleScanSettings {
    return when (this) {
        BleScanSettings.CALLBACK_TYPE_DEFAULT.callbackType -> {
            BleScanSettings.CALLBACK_TYPE_DEFAULT
        }
        BleScanSettings.CALLBACK_TYPE_ALL_MATCHES.callbackType -> {
            BleScanSettings.CALLBACK_TYPE_ALL_MATCHES
        }
        BleScanSettings.CALLBACK_TYPE_FIRST_MATCH.callbackType -> {
            BleScanSettings.CALLBACK_TYPE_FIRST_MATCH
        }
        BleScanSettings.CALLBACK_TYPE_MATCH_LOST.callbackType -> {
            BleScanSettings.CALLBACK_TYPE_MATCH_LOST
        }
        BleScanSettings.CALLBACK_TYPE_REMOVE_BOUND_DEVICE.callbackType -> {
            BleScanSettings.CALLBACK_TYPE_REMOVE_BOUND_DEVICE
        }
        BleScanSettings.CALLBACK_TYPE_LE_SCAN_DEVICE.callbackType -> {
            BleScanSettings.CALLBACK_TYPE_LE_SCAN_DEVICE
        }
        else -> {
            BleScanSettings.CALLBACK_TYPE_DEFAULT
        }
    }
}