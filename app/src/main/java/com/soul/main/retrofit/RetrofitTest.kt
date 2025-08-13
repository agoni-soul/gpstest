package com.soul.main.retrofit

import android.util.Log
import com.google.gson.Gson
import com.soul.coroutineScope.EatGame
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

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

        val observer: Observer<Int> = object : Observer<Int> {
            private var disposable: Disposable? = null

            override fun onNext(value: Int) {
                Log.d(TAG, value.toString())
                if (value >= 2) {   // >=2  时为异常数据，解除订阅
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

        observable.observeOn(Schedulers.newThread()).subscribe(observer) //建立订阅关系
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
