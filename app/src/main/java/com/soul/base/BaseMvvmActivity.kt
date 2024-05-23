package com.soul.base

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider


/**
 *     author : yangzy33
 *     time   : 2024-05-13
 *     desc   :
 *     version: 1.0
 */
abstract class BaseMvvmActivity<V: ViewDataBinding, VM: BaseViewModel>: BaseActivity() {

    protected lateinit var mViewDataBinding: V

    protected lateinit var mViewModel: VM

    protected abstract fun getViewModelClass(): Class<VM>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId())
        val modelClass: Class<VM> = getViewModelClass()
        mViewModel = ViewModelProvider(this).get(modelClass)
        lifecycle.addObserver(mViewModel)
        initView()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewDataBinding.unbind()
        lifecycle.removeObserver(mViewModel)
    }
}