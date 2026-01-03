package com.haha.servicerouter.interfaces

import com.haha.servicerouterannotation.annotation.data.InterceptorMetaData
import java.util.TreeMap

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
interface IInterceptorLoader {
    fun loadInto(map: TreeMap<Int, InterceptorMetaData>)
}