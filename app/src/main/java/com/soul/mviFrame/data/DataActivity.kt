package com.soul.mviFrame.data

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityDataBinding
import com.soul.mviFrame.base.BaseMVIActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @auther: soulagoni
 * @Date:   2025/12/10
 * @Detail:
 */
class DataActivity: BaseMVIActivity<ActivityDataBinding, DataMVIViewModel>() {
    override fun getLayoutId(): Int = R.layout.activity_data

    override fun getViewModelClass(): Class<DataMVIViewModel> = DataMVIViewModel::class.java

    override fun initData() {
        mViewModel.viewModelScope.launch(Dispatchers.Main) {
            mViewModel.dataState.collect {
                render(it)
            }
        }
    }

    private fun render(dataViewState: DataViewState) {

    }

    override fun initView() {
        mViewDataBinding.buttonFetchUser.setOnClickListener {
            mViewModel.viewModelScope.launch {
                mViewModel.dataIntent.send(DataIntent.RequestData("1"))
            }
        }
        mViewDataBinding.progressBar.setOnClickListener {
            mViewModel.viewModelScope.launch {
                mViewModel.dataIntent.send(DataIntent.RequestGuideInfo("12345678"))
            }
        }
    }
}