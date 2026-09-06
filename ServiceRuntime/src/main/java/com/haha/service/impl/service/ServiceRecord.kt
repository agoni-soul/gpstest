package com.haha.service.impl.service

/**
 * 一条「接口 → 实现」注册记录。同一实现 Class 只保留一条，不再按 key 重复。
 */
data class ServiceRecord(
    val interfaceClass: Class<*>,
    val implClass: Class<*>,
    val key: String,
    val singleton: Boolean,
    val isDefault: Boolean,
    val priority: Int
)
