package com.soul.base

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.soul.gpstest.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 *     author : yangzy33
 *     time   : 2024-05-13
 *     desc   :
 *     version: 1.0
 */
abstract class BaseMvvmActivity<V : ViewDataBinding, VM : BaseViewModel> : BaseActivity() {

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

    private var mRequestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    protected abstract fun getViewModelClass(): Class<VM>

    protected abstract fun initView()

    protected abstract fun initData()

    protected open fun isUsedEncapsulatedPermissions(): Boolean = false

    protected open fun requestPermissionArray(): Array<String> = emptyArray()

    protected open fun handlePermissionResult(permissionResultMap: Map<String, Boolean>) {

    }

    protected open fun defaultBackgroundId(): Int = R.color.white

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        mViewDataBinding.root.background = ContextCompat.getDrawable(mContext, defaultBackgroundId())
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

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(TAG, "onRestoreInstanceState")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        mViewDataBinding.unbind()
        lifecycle.removeObserver(mViewModel)
    }
}