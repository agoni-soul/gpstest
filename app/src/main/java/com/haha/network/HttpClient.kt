package com.haha.network

import android.app.Application
import com.haha.hahalearn.BuildConfig
import com.haha.log.DOFLogUtil
import com.haha.network.HttpClient.create
import com.haha.network.HttpClient.init
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 统一 Retrofit + RxJava2 + OkHttp。
 * 启动表 [com.haha.startup.task.NetworkInitTask] 调用 [init] 后用 [create] 拿 Service。
 */
object HttpClient {
    private const val TAG = "HttpClient"
    private const val DEFAULT_BASE_URL = "https://api.github.com/"
    private const val CACHE_BYTES = 20L * 1024 * 1024

    @Volatile
    private var initialized = false

    lateinit var okHttpClient: OkHttpClient
        private set

    lateinit var retrofit: Retrofit
        private set

    fun init(app: Application, baseUrl: String = DEFAULT_BASE_URL) {
        if (initialized) {
            return
        }
        synchronized(this) {
            if (initialized) {
                return
            }
            okHttpClient = OkHttpClient.Builder()
                .cache(Cache(File(app.cacheDir, "http_cache"), CACHE_BYTES))
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(HttpLogInterceptor())
                .build()
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
            initialized = true
            DOFLogUtil.d(TAG, "initialized, baseUrl=$baseUrl")
        }
    }

    fun isInitialized(): Boolean = initialized

    fun <T> create(service: Class<T>): T {
        check(initialized) { "HttpClient.init 须先由 NetworkInitTask 执行" }
        return retrofit.create(service)
    }
}

private class HttpLogInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val start = System.nanoTime()
        val response = chain.proceed(request)
        if (BuildConfig.DEBUG) {
            val ms = (System.nanoTime() - start) / 1_000_000
            DOFLogUtil.d(
                "HttpClient",
                "${request.method} ${request.url} -> ${response.code} ${ms}ms"
            )
        }
        return response
    }
}
