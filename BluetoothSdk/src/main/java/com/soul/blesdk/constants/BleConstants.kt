package com.soul.blesdk.constants

import java.util.*


/**
 *     author : haha
 *     time   : 2024-07-31
 *     desc   :
 *     version: 1.0
 */
object BleConstants {
    val BLUE_UUID: UUID = UUID.fromString("00001101-2300-1000-8000-00815F9B34FB")
    val UUID_SERVICE: UUID = UUID.fromString("10000000-0000-0000-0000-000000000000")
    val UUID_READ_NOTIFY: UUID = UUID.fromString("11000000-0000-0000-0000-000000000000")
    val UUID_WRITE: UUID = UUID.fromString("12000000-0000-0000-0000-000000000000")
    val UUID_DESCRIBE: UUID = UUID.fromString("12000000-0000-0000-0000-000000000000")
}