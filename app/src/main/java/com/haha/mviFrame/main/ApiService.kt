package com.haha.mviFrame.main

import retrofit2.http.GET

/**
 * @auther: haha
 * @Date:   2025/12/7
 * @Detail:
 */
interface ApiService {

    @GET("users")
    suspend fun getUsers(): List<User>
}

