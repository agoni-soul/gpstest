package com.soul.base

import android.os.Bundle
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.soul.log.DOFLogUtil


/**
 *     author : yangzy33
 *     time   : 2024-05-13
 *     desc   :
 *     version: 1.0
 */
abstract class BaseMvvmActivity<V: ViewDataBinding, VM: BaseViewModel>: BaseActivity() {

    protected var mViewDataBinding: V? = null

    protected var mViewModel: VM? = null

    protected abstract fun getViewModelClass(): Class<VM>?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId())
        if (!isShowStatus()) {
            addStatusBarView()
        }
        val modelClass: Class<VM>? = getViewModelClass()
        modelClass?.let {
            mViewModel = ViewModelProvider(this).get(it)
        }
        mViewModel?.let {
            lifecycle.addObserver(it)
        }
        initView()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewDataBinding?.unbind()
        mViewDataBinding = null
        mViewModel?.let {
            lifecycle.removeObserver(it)
        }
    }
}