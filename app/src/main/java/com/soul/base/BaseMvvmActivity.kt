package com.soul.base

import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-05-13
 *     desc   :
 *     version: 1.0
 */
abstract class BaseMvvmActivity<V : ViewDataBinding, VM : BaseViewModel> : BaseActivity() {

    protected val mViewDataBinding: V by lazy {
        DataBindingUtil.setContentView(this, getLayoutId())
    }

    protected val mViewModel: VM by lazy {
        val modelClass: Class<VM> = getViewModelClass()
        val viewModel = ViewModelProvider(this)[modelClass]
        lifecycle.addObserver(viewModel)
        viewModel
    }

    private var mRequestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    protected abstract fun getViewModelClass(): Class<VM>

    protected abstract fun initView()

    protected abstract fun initData()

    protected open fun isUsedEncapsulatedPermissions(): Boolean = false

    protected open fun requestPermissionArray(): Array<String> = emptyArray()

    protected open fun handlePermissionResult(permissionResultMap: Map<String, Boolean>) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewDataBinding.root.background = ContextCompat.getDrawable(mContext, R.color.white)
        if (!isShowStatus()) {
            addStatusBarView()
        }
        if (isUsedEncapsulatedPermissions()) {
            mRequestPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionResultMap ->
                    handlePermissionResult(permissionResultMap)
                }
        }
        mRequestPermissionLauncher?.launch(requestPermissionArray())
        initView()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewDataBinding.unbind()
        lifecycle.removeObserver(mViewModel)
    }
}