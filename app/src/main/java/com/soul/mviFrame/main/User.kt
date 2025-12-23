package com.soul.mviFrame.main

import com.squareup.moshi.Json

/**
 * @auther: soulagoni
 * @Date:   2025/12/7
 * @Detail:
 */
data class User(
    @Json(name = "id")
    val id: Int = 0,
    @Json(name = "first_name")
    val name: String = "",
    @Json(name = "email")
    val email: String = "",
    @Json(name = "avator")
    val avator: String = ""
)