package com.haha.servicerouterannotation.annotation

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
enum class RouteType(val className: String) {
    ACTIVITY("android.app.activity"), FRAGMENT("android.app.Fragment"),
    FRAGMENT_X("androidx.fragment.app.Fragment"), UNKNOWN("")
}