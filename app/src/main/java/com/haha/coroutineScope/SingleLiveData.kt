package com.haha.coroutineScope

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * @auther: haha
 * @Date:   2026/8/23
 * @Detail:
 */
class SingleLiveData<T> : MutableLiveData<T>() {

    private val TAG = "SingleLiveData"

    @OptIn(ExperimentalAtomicApi::class)
    private var mIsFirst = AtomicBoolean(false)

    @OptIn(ExperimentalAtomicApi::class)
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.")
        }
        super.observe(owner, Observer { value ->
            if (mIsFirst.compareAndSet(expectedValue = true, newValue = false)) {
                observer.onChanged(value)
            }
        })
    }

    @OptIn(ExperimentalAtomicApi::class)
    override fun setValue(value: T?) {
        mIsFirst = AtomicBoolean(true)
        super.value = value
    }


}