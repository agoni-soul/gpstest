package com.soul.base

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.soul.gpstest.R
import com.soul.main.timeMonitor.TimeMonitorConfig
import com.soul.main.timeMonitor.TimeMonitorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 *     author : yangzy33
 *     time   : 2024-05-13
 *     desc   :
 *     version: 1.0
 */
abstract class BaseMvvmActivity<V : ViewDataBinding, VM : BaseViewModel> : BaseActivity() {

    private var _binding: V? = null
    protected val mViewDataBinding: V
        get() = _binding
            ?: error("binding 未初始化，须先走 inflateContentView / bindInflatedContentView")

    protected fun isBindingInitialized(): Boolean = _binding != null

    protected val mViewModel: VM by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val modelClass: Class<VM> = getViewModelClass()
        val viewModel = ViewModelProvider(this)[modelClass]
        viewModel.viewModelScope.launch(Dispatchers.Main) {
            lifecycle.addObserver(viewModel)
        }
        ViewModelProvider(this).get(modelClass)
        viewModel
    }

    private var mRequestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    override fun inflateContentView() {
        _binding = DataBindingUtil.setContentView(this, getLayoutId())
        _binding?.lifecycleOwner = this
    }

    /**
     * 异步 inflate 完成后，在主线程 bind + setContentView。
     * 不可在后台线程调用。
     */
    protected fun bindInflatedContentView(contentView: View) {
        _binding = DataBindingUtil.bind(contentView)
            ?: error("DataBinding bind 失败，确认布局根节点是 <layout>")
        _binding?.lifecycleOwner = this
        setContentView(contentView)
        // 异步 inflate 时 onCreate 里跳过了系统栏处理，此处 DecorView 已就绪再补上
        handleNavigationVAndStatusVisibility()
    }

    /**
     * 内容 View 就绪后的 MVVM 初始化（背景、状态栏占位、权限、initView/initData）。
     */
    protected open fun onContentReady() {
        TimeMonitorManager.getInstance()
            .getTimeMonitor(TimeMonitorConfig.TIME_MONITOR_ID_APPLICATION_START)
            .recodingTimeTag("BaseMvvmActivity_create")

        mViewDataBinding.root.background = ContextCompat.getDrawable(mContext, defaultBackgroundId())
        if (!isShowStatus()) {
            addStatusBarView()
        }
        mRequestPermissionLauncher?.launch(requestPermissionArray())

        TimeMonitorManager.getInstance()
            .getTimeMonitor(TimeMonitorConfig.TIME_MONITOR_ID_APPLICATION_START)
            .recodingTimeTag("BaseMvvmActivity_initView_before")
        initView()
        TimeMonitorManager.getInstance()
            .getTimeMonitor(TimeMonitorConfig.TIME_MONITOR_ID_APPLICATION_START)
            .recodingTimeTag("BaseMvvmActivity_initData_before")
        initData()
    }

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

        // registerForActivityResult 必须在 STARTED 之前，不能等到异步 inflate 回调
        if (isUsedEncapsulatedPermissions()) {
            mRequestPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionResultMap ->
                    handlePermissionResult(permissionResultMap)
                }
        }
        if (shouldInflateContentInOnCreate()) {
            onContentReady()
        }
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
        _binding?.unbind()
        _binding = null
        super.onDestroy()
    }
}