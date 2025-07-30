package com.soul.main.retrofit

import android.util.Log
import com.soul.coroutineScope.EatGame
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Call
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
    fun test() {
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
                Log.d("xujun", value.toString())
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
}

interface ApiService {
    @GET("users/{user}/repos")
    fun listRepos(@Path("user") user: String?): Call<List<EatGame?>?>?
}
