package com.soul.blesdk.exceptions


/**
 *     author : haha
 *     time   : 2024-07-31
 *     desc   :
 *     version: 1.0
 */
class BleErrorException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause) {
    constructor(message: String?) : this(message, null)
    constructor() : this(null, null)
    constructor(cause: Throwable?) : this(null, cause)
}