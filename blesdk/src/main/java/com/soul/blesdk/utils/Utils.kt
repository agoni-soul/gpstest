package com.soul.blesdk.utils

import java.io.Closeable


/**
 *     author : yangzy33
 *     time   : 2024-07-29
 *     desc   :
 *     version: 1.0
 */
fun close(vararg closeable: Closeable?) {
    closeable?.forEach { obj -> obj?.close() }
}