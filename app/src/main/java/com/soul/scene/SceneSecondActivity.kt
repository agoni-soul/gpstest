package com.soul.scene

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.soul.base.BaseActivity
import com.soul.base.BaseMvvmActivity
import com.soul.base.BaseViewModel
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivitySceneSecondBinding

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/10/27
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class SceneSecondActivity : BaseMvvmActivity<ActivitySceneSecondBinding, BaseViewModel>() {
    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_scene_second

    override fun initView() {
    }

    override fun initData() {
    }
}