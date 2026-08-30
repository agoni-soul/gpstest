package com.haha.main.retrofit

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 *
 * @author:     haha
 * @date:       2026/8/30
 * Description: OkHttp 正式用法示例：共享 Client + 异步 GET / POST
 *
 **/
class OkHttpTest {
    private val TAG = this.javaClass.simpleName

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "HahaLearn")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    fun test() {
        get("https://api.github.com/users/octocat")
        post("https://httpbin.org/post")
    }

    fun get(url: String) {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "get onFailure: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string()
                    if (!it.isSuccessful) {
                        Log.e(TAG, "get http ${it.code}, body=$body")
                        return
                    }
                    Log.d(TAG, "get result: $body")
                }
            }
        })
    }

    fun post(url: String) {
        val requestBody = FormBody.Builder()
            .add("city", "长沙")
            .add("name", "haha")
            .build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "post onFailure: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string()
                    if (!it.isSuccessful) {
                        Log.e(TAG, "post http ${it.code}, body=$body")
                        return
                    }
                    Log.d(TAG, "post result: $body")
                }
            }
        })
    }
}
