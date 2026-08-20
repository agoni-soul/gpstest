package com.haha.mviFrame.main

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * @auther: haha
 * @Date:   2025/12/7
 * @Detail:
 */
object RetrofitBuilder {

    private const val BASE_URL = "https://reqres.in/api/user/1/"

    private fun getRetrofit() = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()


    val apiService: ApiService = getRetrofit().create(ApiService::class.java)

}


