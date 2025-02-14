package com.soul.coroutineScope

import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.soul.base.BaseMvvmActivity
import com.soul.base.BaseViewModel
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityCoroutineScopeBinding
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
class CoroutineScopeActivity: BaseMvvmActivity<ActivityCoroutineScopeBinding, CoroutineScopeViewModel>() {

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

    override fun getViewModelClass(): Class<CoroutineScopeViewModel> = CoroutineScopeViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_coroutine_scope

    override fun initView() {
        mBtnStart.setOnClickListener {
            mViewModel.startScan()
        }
        mBtnCancel.setOnClickListener {
            mViewModel.stopScan()
        }
        mViewModel.mSsidFirstData.observe(this) {
            mTvFirstScope.text = it
        }
        mViewModel.mSsidSecondData.observe(this) {
            mTvSecondScope.text = it
        }
    }

    override fun initData() {
    }
}