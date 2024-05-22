package com.soul.slidedelete

import com.soul.base.BaseMvvmActivity
import com.soul.base.BaseViewModel
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivitySceneSecondBinding
import com.soul.gpstest.databinding.ActivitySlideDeleteBinding


/**
 *     author : yangzy33
 *     time   : 2024-05-22
 *     desc   :
 *     version: 1.0
 */
class SlideDeleteActivity: BaseMvvmActivity<ActivitySlideDeleteBinding, BaseViewModel>() {
    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_slide_delete

    override fun initView() {
        TODO("Not yet implemented")
    }

    override fun initData() {
        TODO("Not yet implemented")
    }
}