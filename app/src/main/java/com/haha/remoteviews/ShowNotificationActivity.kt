package com.haha.remoteviews

import com.haha.base.BaseMvvmActivity
import com.haha.base.BaseViewModel
import com.haha.hahalearn.R
import com.haha.hahalearn.databinding.ActivityShowNotificationBinding

class ShowNotificationActivity : BaseMvvmActivity<ActivityShowNotificationBinding, BaseViewModel>() {
    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_show_notification

    override fun initView() {
    }

    override fun initData() {
    }
}