package com.haha.servicerouterannotation.annotation.data

import javax.annotation.Priority

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
data class InterceptorMetaData(val priority: Int, val elementType: String, val clazz: Class<Any>)