package com.soul.mviFrame.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.soul.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @auther: soulagoni
 * @Date:   2025/12/10
 * @Detail:
 */
abstract class BaseMVIActivity<V: ViewDataBinding, VM: BaseMVIViewModel>: AppCompatActivity() {
    protected val mViewDataBinding: V by lazy(LazyThreadSafetyMode.PUBLICATION) {
        DataBindingUtil.setContentView(this, getLayoutId())
    }

    protected val mViewModel: VM by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val modelClass: Class<VM> = getViewModelClass()
        val viewModel = ViewModelProvider(this)[modelClass]
        viewModel.viewModelScope.launch(Dispatchers.Main) {
            lifecycle.addObserver(viewModel)
        }
        viewModel
    }

    protected abstract fun getLayoutId(): Int

    protected abstract fun getViewModelClass(): Class<VM>

    protected abstract fun initData()

    protected abstract fun initView()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewDataBinding.unbind()
        lifecycle.removeObserver(mViewModel)
    }
}