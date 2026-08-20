package com.haha.mviFrame.main

/**
 * @auther: haha
 * @Date:   2025/12/7
 * @Detail:
 */
class MainRepository(private val apiService: ApiService) {

    suspend fun getUsers() = apiService.getUsers()

}
