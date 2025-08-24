package com.soul.main.retrofit

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.soul.coroutineScope.EatGame
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Dispatcher
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.cache.CacheInterceptor
import okhttp3.internal.connection.ConnectInterceptor
import okhttp3.internal.http.BridgeInterceptor
import okhttp3.internal.http.CallServerInterceptor
import okhttp3.internal.http.RetryAndFollowUpInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.File
import java.io.IOException

/**
 *
 * @author:     haha
 * @date:       2025/7/30
 * Description: retrofit第三方接口测试
 *
 **/
class RetrofitTest {
    private val TAG = this.javaClass.simpleName

    fun test() {
        retrofitTest()
        observerTest()
        methodTest()
    }

    private fun retrofitTest() {
        // 1. 创建retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        // 2. 创建代理对象
        val service = retrofit.create(ApiService::class.java)

        // 3. 调用请求方法
        val repos = service.listRepos(mutableListOf("octocat"))

        // 4. 发起请求并获取回调
        repos?.enqueue(object : Callback<List<EatGame?>?> {
            override fun onResponse(call: Call<List<EatGame?>?>, response: Response<List<EatGame?>?>) {
                Log.e(TAG, "result: " + response.body())
            }

            override fun onFailure(call: Call<List<EatGame?>?>, t: Throwable) {
                Log.e(TAG, "onFailure: $t")
            }
        })
    }

    private fun observerTest() {
        // 被观察者
        val observable: Observable<Int> = Observable.create(object : ObservableOnSubscribe<Int> {
            @Throws(java.lang.Exception::class)
            override fun subscribe(e: ObservableEmitter<Int>) {
                e.onNext(1)
                e.onNext(2)
                e.onNext(3)
                e.onNext(4)
                e.onComplete()
            }
        })

        // 观察者
        val observer: Observer<String> = object : Observer<String> {
            private var disposable: Disposable? = null

            override fun onNext(value: String) {
                Log.d(TAG, value.toString())
                if (value.toInt() >= 2) {   // >=2  时为异常数据，解除订阅
                    disposable?.dispose()
                }
            }

            override fun onSubscribe(d: Disposable) {
                disposable = d
            }

            override fun onError(e: Throwable) {
            }

            override fun onComplete() {
            }
        }

        observable.observeOn(Schedulers.newThread())
            .map(object: Function<Int, String> {
                override fun apply(t: Int): String {
                    return t.toString()
                }
            })
            .subscribe(observer) //建立订阅关系
    }

    private fun okHttpTest(context: Context) {
        val dispatcher = Dispatcher()
        dispatcher.maxRequests = 65
        val file = File(context.cacheDir, "okhttp_cache")
        val client = OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .cache(Cache(file, 10 * 1024 * 1024))
            .addInterceptor(object: Interceptor {
                override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                    val originalRequest = chain.request()
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer token")
                        .build()
                    return chain.proceed(newRequest);
                }
            })
            .build()
        get("http://www.baidu.com", client)
    }

    private fun get(url: String, client: OkHttpClient) {
        try {
            val request = Request.Builder()
                .url(url)
                .build()

            // 执行同步请求
            val call = client.newCall(request)
            val response = call.execute()

            // 获取响应
            val body = response.body
            Log.d(TAG, "$body")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun post(url: String, client: OkHttpClient) {
        try {
            val requestBody = FormBody.Builder()
                .add( "city", "长沙")
                .add("key", "13cb58f5884f9749287abbead9c658f2")
                .build()
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            // 执行同步请求
            val call = client.newCall(request)
            val response = call.execute()
            call.enqueue(object: okhttp3.Callback {
                /**
                 * Called when the request could not be executed due to cancellation, a connectivity problem or
                 * timeout. Because networks can fail during an exchange, it is possible that the remote server
                 * accepted the request before the failure.
                 */
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    TODO("Not yet implemented")
                }

                /**
                 * Called when the HTTP response was successfully returned by the remote server. The callback may
                 * proceed to read the response body with [Response.body]. The response is still live until its
                 * response body is [closed][ResponseBody]. The recipient of the callback may consume the response
                 * body on another thread.
                 *
                 * Note that transport-layer success (receiving a HTTP response code, headers and body) does not
                 * necessarily indicate application-layer success: `response` may still indicate an unhappy HTTP
                 * response code like 404 or 500.
                 */
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    TODO("Not yet implemented")
                }

            })

            // 获取响应
            val body = response.body
            Log.d(TAG, "$body")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun methodTest() {
        try {
            val clazz = Class.forName("com.soul.main.retrofit.ApiService")
            val method = clazz.getDeclaredMethod("listRepos", List::class.java)
            Log.d(TAG, " --------------  annotation --------------  ")
            for (annotation in method.annotations) {
                Log.d(TAG, "$annotation")
            }
            Log.d(TAG, " --------------  parameterTypes --------------  ")
            for (type in method.parameterTypes) {
                Log.d(TAG, "${type.simpleName}")
            }
            Log.d(TAG, " --------------  genericParameterTypes --------------  ")
            for (type in method.genericParameterTypes) {
                Log.d(TAG, "${type.typeName}")
            }
            Log.d(TAG, " --------------  genericReturnType --------------  ")
            Log.d(TAG, "${method.genericReturnType.typeName}")
            Log.d(TAG, " --------------  end --------------  ")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

interface ApiService {
    @GET("users/{user}/repos")
    fun listRepos(@Path("user") user: List<String>?): Call<List<EatGame?>?>?
}
