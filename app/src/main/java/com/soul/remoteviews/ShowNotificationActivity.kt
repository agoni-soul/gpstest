package com.soul.remoteviews

import com.soul.base.BaseMvvmActivity
import com.soul.base.BaseViewModel
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityShowNotificationBinding

class ShowNotificationActivity : BaseMvvmActivity<ActivityShowNotificationBinding, BaseViewModel>() {
    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_show_notification

    override fun initView() {
    }

    override fun initData() {
    }
}