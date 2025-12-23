package com.soul.mviFrame.main

import retrofit2.http.GET

/**
 * @auther: soulagoni
 * @Date:   2025/12/7
 * @Detail:
 */
interface ApiService {

    @GET("users")
    suspend fun getUsers(): List<User>
}

