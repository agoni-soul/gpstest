package com.soul.coroutineScope

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.soul.base.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 *
 * @author:     haha
 * @date:       2025/2/11
 * Description: 协程数据Model
 *
 **/
class CoroutineScopeViewModel(application: Application): BaseViewModel(application) {

    private var mSSID = "abcd"
    @Volatile
    private var mIsPreciseMatch: Boolean = false
    private var mScanDeviceJob : Job? = null
    private var mFindDeviceJob : Job? = null

    private var scopeTest: CoroutineScopeTest? = null

    private val _mSsidFirstData: MutableLiveData<String> = MutableLiveData()
    val mSsidFirstData: LiveData<String> = _mSsidFirstData

    private val _mSsidSecondData: MutableLiveData<String> = MutableLiveData()
    val mSsidSecondData: LiveData<String> = _mSsidSecondData

    private val _mediatorLiveData: MediatorLiveData<String> = MediatorLiveData()
    val mediatorLiveData: LiveData<String> = _mediatorLiveData

    init {
        _mediatorLiveData.addSource<String>(
            _mSsidFirstData,
            Observer<String> {
                _mediatorLiveData.value = it
            })
        _mediatorLiveData.addSource<String>(
            _mSsidSecondData,
            Observer<String> {
                _mediatorLiveData.value = it
            })
    }

    fun startScan() {
        val array = initScanData()
        for (s in array) {
            filterDevice(s)
        }

        scopeTest = CoroutineScopeTest()
        scopeTest?.start()
         val mediatorLiveData = MediatorLiveData<String>()
        mediatorLiveData.addSource(mSsidFirstData) {
            Log.d(TAG, "onChange1: ${it}")
        }
        _mSsidSecondData.postValue("haha")
    }

    private fun initScanData(): MutableList<String> {
        val array = ArrayList<String>()
        array.add("abce")
        array.add("abeef")
        array.add("abcefd")
        array.add("abcefd")
        array.add("abafdce")
        array.add("abcfdae")
        array.add("abcefad")
        array.add("abdefdce")
        array.add("addbce")
        array.add("abce")
        array.add("adfdbce")
        array.add("addbce")
        array.add("addbce")
        array.add("adbce")
        array.add("abcDEd")
        array.add("abcfdfdde")
        array.add("abcDed")
        array.add("abcDED")
        array.add("abcDEd")
        array.add("abcefd")
        array.add("abcefd")
        array.add("abafdce")
        array.add("abcfdae")
        array.add("abcefad")
        array.add("abdefdce")
        return array
    }

    private fun filterDevice(s: String) {
        mScanDeviceJob = CoroutineScope(Dispatchers.Default).launch {
            if (mIsPreciseMatch) {
                runBlocking {
                    Log.d(TAG, "mIsPreciseMatch = $mIsPreciseMatch, mSSID = $mSSID, s = $s, ${Thread.currentThread()} hahahahh")
                    if (mIsPreciseMatch) {
                        if (mSSID == s) {
                            GlobalScope.launch(Dispatchers.Main) {
                                if (mIsPreciseMatch) {
                                    _mSsidFirstData.postValue(s)
                                    Log.d(TAG, "mIsPreciseMatch = $mIsPreciseMatch, mSSID = $mSSID, s = $s, ${Thread.currentThread()} hahahahh")
                                }
                            }
                        }
                    }
                }
            } else {
                runBlocking {
                    Log.d(TAG, "mIsPreciseMatch = $mIsPreciseMatch, mSSID = $mSSID, s = $s, ${Thread.currentThread()} mFindDeviceJob")
                    if (!mIsPreciseMatch) {
                        if (s.contains(mSSID, true)) {
                            GlobalScope.launch(Dispatchers.Main) {
                                if (!mIsPreciseMatch) {
                                    mIsPreciseMatch = true
                                    mSSID = s
                                    _mSsidSecondData.postValue(s)
                                    Log.d(TAG, "mIsPreciseMatch = $mIsPreciseMatch, mSSID = $mSSID, s = $s, Dispatchers.Main mFindDeviceJob")
                                }
                            }
                        }
                    }
                }
            }

//            synchronized(mIsPreciseMatch) {
//                Log.d(TAG, "mIsPreciseMatch = $mIsPreciseMatch, mSSID = $mSSID, s = $s, Dispatchers.Default hahahahh")
//                if (mIsPreciseMatch) {
//                    if (mSSID == s) {
//                        GlobalScope.launch(Dispatchers.Main) {
//                            if (mIsPreciseMatch) {
//                                mTvSecondScope.text = s
//                                Log.d(TAG, "mIsPreciseMatch = $mIsPreciseMatch, mSSID = $mSSID, s = $s, Dispatchers.Main hahahahh")
//                            }
//                        }
//                    }
//                } else {
//                    Log.d(TAG, "mIsPreciseMatch = $mIsPreciseMatch, mSSID = $mSSID, s = $s, Dispatchers.IO mFindDeviceJob")
//                    if (s.contains(mSSID, true)) {
//                        GlobalScope.launch(Dispatchers.Main) {
//                            if (!mIsPreciseMatch) {
//                                mIsPreciseMatch = true
//                                mSSID = s
//                                mTvFirstScope.text = s
//                                Log.d(TAG, "mIsPreciseMatch = $mIsPreciseMatch, mSSID = $mSSID, s = $s, Dispatchers.Main mFindDeviceJob")
//                            } else {
//                                Log.d(TAG, "mIsPreciseMatch = $mIsPreciseMatch, mSSID = $mSSID, s = $s, Dispatchers.IO 异步线程")
//                            }
//                        }
//                    }
//                }
//            }
            if (mIsPreciseMatch) {
//                mFindDeviceJob?.let {
//                    if (it.isActive) {
//                        Log.d(TAG, "mIsPreciseMatch = $mIsPreciseMatch, mSSID = $mSSID, s = $s, mFindDeviceJob 取消")
//                        it.cancelChildren()
//                    }
//                }
            } else {
            }
        }
    }

    fun stopScan() {
        mScanDeviceJob?.cancelChildren()
        scopeTest?.stop()
    }
}