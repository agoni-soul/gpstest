package com.soul.liveData

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 *
 * @author:     haha
 * @date:       2025/3/26
 * Description:
 *
 **/
class NonStickyMutableLiveData<T> : MutableLiveData<T>() {
    private var mStickFlag = false

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, observer)
        if (!mStickFlag) {
            hook(observer)
        }
    }

    private fun hook(observer: Observer<in T>) {
        try {
            val liveDataClass: Class<LiveData<*>> = LiveData::class.java
            val mObserversField = liveDataClass.getDeclaredField("mObservers")
            mObserversField.isAccessible = true
            val mObserversObject = mObserversField.get(this)
            val mObserverClass = mObserversObject.javaClass
            val get = mObserverClass.getDeclaredMethod("get", Observer::class.java)
            get.isAccessible = true
            val invokeEntry = get.invoke(mObserversObject, observer)
            var observerWrapper: Any? = null
            if (invokeEntry != null && invokeEntry is Map.Entry<*, *>) {
                observerWrapper = invokeEntry.value
            }
            if (observerWrapper == null) {
                throw NullPointerException("observerWrapper is null")
            }
            val superClass: Class<in Any> = observerWrapper.javaClass.superclass
            val mLastVersion = superClass.getDeclaredField("mLastVersion")
            mLastVersion.isAccessible = true
            val mVersion = liveDataClass.getDeclaredField("mVersion")
            mVersion.isAccessible = true
            val mVersionValue = mVersion.get(this)
            mLastVersion.set(observerWrapper, mVersionValue)

            mStickFlag = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}