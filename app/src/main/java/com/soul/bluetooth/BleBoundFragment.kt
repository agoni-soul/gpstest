package com.soul.bluetooth

import com.soul.base.BaseMvvmFragment
import com.soul.base.BaseViewModel
import com.soul.gpstest.databinding.FragmentBleScanBinding


/**
 *     author : yangzy33
 *     time   : 2024-08-13
 *     desc   :
 *     version: 1.0
 */
class BleBondFragment: BaseMvvmFragment<FragmentBleScanBinding, BaseViewModel>() {
    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun getLayoutId(): Int {
        TODO("Not yet implemented")
    }

    override fun initView() {
    }

    override fun initData() {
    }
}