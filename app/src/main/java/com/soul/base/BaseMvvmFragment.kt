package com.soul.base

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-08-12
 *     desc   :
 *     version: 1.0
 */
abstract class BaseMvvmFragment<V: ViewDataBinding, VM: BaseViewModel>: BaseFragment() {

    protected lateinit var mViewDataBinding: V

    protected lateinit var mViewModel: VM

    protected abstract fun getViewModelClass(): Class<VM>?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewDataBinding = DataBindingUtil.setContentView(requireActivity(), getLayoutId())
        mViewDataBinding.root.background =  ContextCompat.getDrawable(mContext, R.color.white)
        if (!isShowStatus()) {
            addStatusBarView()
        }
        val modelClass: Class<VM>? = getViewModelClass()
        modelClass?.let {
            mViewModel = ViewModelProvider(this).get(it)
        }
        mViewModel.let {
            lifecycle.addObserver(it)
        }
        initView()
        initData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewDataBinding.unbind()
        mViewModel.let {
            lifecycle.removeObserver(it)
        }
    }
}