package com.haha.servicerouter.interfaces

import com.haha.servicerouterannotation.annotation.data.RouteMetaData

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
interface IRouteLoader {
    fun loadInto(map: MutableMap<String, RouteMetaData>)
}