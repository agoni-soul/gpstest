package com.soul.coroutineScope

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.soul.base.BaseActivity
import com.soul.gpstest.R
import kotlinx.coroutines.*

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/10/25
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class CoroutineScopeActivity: BaseActivity() {

    private val mBtnStart: Button by lazy {
        findViewById(R.id.btn_start_coroutine_scope)
    }

    private val mBtnCancel: Button by lazy {
        findViewById(R.id.btn_cancel_coroutine_scope)
    }

    private val mTvFirstScope: TextView by lazy {
        findViewById(R.id.tv_coroutine_scope_first)
    }

    private val mTvSecondScope: TextView by lazy {
        findViewById(R.id.tv_coroutine_scope_second)
    }

    override fun getLayoutId(): Int = R.layout.activity_coroutine_scope

    override fun initView() {
        mBtnStart.setOnClickListener {
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
            for (s in array) {
                filterDevice(s)
            }
        }
        mBtnCancel.setOnClickListener {
            mScanDeviceJob?.cancelChildren()
        }
    }

    override fun initData() {
    }

    private var mSSID = "abcd"
    @Volatile
    private var mIsPreciseMatch: Boolean = false
    private var mScanDeviceJob : Job? = null
    private var mFindDeviceJob : Job? = null

    private fun filterDevice(s: String) {
        mScanDeviceJob = CoroutineScope(Dispatchers.Default).launch {
            if (mIsPreciseMatch) {
                runBlocking {
                    Log.d(TAG, "mIsPreciseMatch = $mIsPreciseMatch, mSSID = $mSSID, s = $s, ${Thread.currentThread()} hahahahh")
                    if (mIsPreciseMatch) {
                        if (mSSID == s) {
                            GlobalScope.launch(Dispatchers.Main) {
                                if (mIsPreciseMatch) {
                                    mTvSecondScope.text = s
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
                                    mTvFirstScope.text = s
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
}