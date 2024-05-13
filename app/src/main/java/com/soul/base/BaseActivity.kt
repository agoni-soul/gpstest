package com.soul.base

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


/**
 *     author : yangzy33
 *     time   : 2024-05-11
 *     desc   :
 *     version: 1.0
 */
abstract class BaseActivity: AppCompatActivity() {
    protected open val TAG = javaClass.simpleName

    abstract fun getLayoutId(): Int

    abstract fun initView()

    abstract fun initData()

    protected lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
    }
}