package com.soul.volume

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.soul.base.BaseViewModel


/**
 *     author : yangzy33
 *     time   : 2024-05-14
 *     desc   :
 *     version: 1.0
 */
class VolumeViewModel(application: Application): BaseViewModel(application) {
    private val mIsMediaPrepareLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData(false)
    }

    fun getIsMediaPrepare(): MutableLiveData<Boolean> = mIsMediaPrepareLiveData
}