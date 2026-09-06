package com.haha.servicerouter.interfaces

import com.haha.servicerouterannotation.annotation.data.InterceptorMetaData

/**
 * 按模块装拦截器。用 List 而不是 TreeMap：同 priority 可以共存。
 */
interface IInterceptorLoader {
    fun loadInto(list: MutableList<InterceptorMetaData>)
}