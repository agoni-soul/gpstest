package com.soul.mviFrame.main

/**
 * @auther: soulagoni
 * @Date:   2025/12/7
 * @Detail:
 */
class MainRepository(private val apiService: ApiService) {

    suspend fun getUsers() = apiService.getUsers()

}
