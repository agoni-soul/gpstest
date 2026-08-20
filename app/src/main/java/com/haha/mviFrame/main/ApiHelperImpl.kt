package com.haha.mviFrame.main

/**
 * @auther: haha
 * @Date:   2025/12/7
 * @Detail:
 */
class ApiHelperImpl(private val apiService: ApiService) : ApiService {
    override suspend fun getUsers(): List<User> {
        val list = mutableListOf<User>()
        list.addAll(apiService.getUsers())
        for (i in 0..5) {
            val user = User(
                i,
                "haha_$i",
                "haha_nihao${i}@qq.com",
                "https://example.com/avatar_$i.png"
            )
            list.add(user)
        }
        return list
    }
}