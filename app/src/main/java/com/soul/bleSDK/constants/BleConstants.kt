package com.soul.bleSDK.constants

import java.util.*


/**
 *     author : yangzy33
 *     time   : 2024-07-31
 *     desc   :
 *     version: 1.0
 */
object BleConstants {
    val BLUE_UUID = UUID.fromString("00001101-2300-1000-8000-00815F9B34FB")
    val UUID_SERVICE = UUID.fromString("10000000-0000-0000-0000-000000000000")
    val UUID_READ_NOTIFY = UUID.fromString("11000000-0000-0000-0000-000000000000")
    val UUID_WRITE = UUID.fromString("12000000-0000-0000-0000-000000000000")
    val UUID_DESCRIBE = UUID.fromString("12000000-0000-0000-0000-000000000000")
}