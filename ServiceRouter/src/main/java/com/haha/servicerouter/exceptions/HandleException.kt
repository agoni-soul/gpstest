package com.haha.servicerouter.exceptions

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
open class HandleException: RuntimeException {

    constructor(msg: String): super(msg)

    constructor(throwable: Throwable): super(throwable)
}