package com.soul.mediaplayer

import com.soul.base.BaseMvvmActivity
import com.soul.base.BaseViewModel
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityMediaPlayerBinding


/**
 *     author : yangzy33
 *     time   : 2024-05-17
 *     desc   :
 *     version: 1.0
 */
class MediaPlayerActivity : BaseMvvmActivity<ActivityMediaPlayerBinding, BaseViewModel>() {
    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_media_player

    override fun initView() {

    }

    override fun initData() {

    }
}