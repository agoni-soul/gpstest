package com.soul.base

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())

        initView()
        initData()
    }
}