package com.soul.mviFrame.main

/**
 * @auther: soulagoni
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
                "/Users/soulagoni/AndroidStudioProjects/GPSTest/gpstest/app/src/main/res/drawable/ic_play_mode_shuffle.png"
            )
            list.add(user)
        }
        return list
    }
}