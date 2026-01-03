package com.haha.servicerouter.exceptions

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
class RouteNotFoundException: HandleException {

    constructor(msg: String): super(msg)

    constructor(throwable: Throwable): super(throwable)
}