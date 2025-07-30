package com.soul.liveData

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.soul.base.BaseMvvmActivity
import com.soul.base.BaseViewModel
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityLiveDataBinding

/**
 *
 * @author:     haha
 * @date:       2025/3/26
 * Description:
 *
 **/
class LiveDataActivity : BaseMvvmActivity<ActivityLiveDataBinding, BaseViewModel>() {
    companion object {
        var mLiveData: MutableLiveData<String>? = null
    }

    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun initView() {
        mViewDataBinding.btnMainSend.setOnClickListener {
            mLiveData?.value = "main send"
        }
        mViewDataBinding.btnThreadSend.setOnClickListener {
            Thread {
                mLiveData?.postValue("thread send")
            }.start()
        }
        mViewDataBinding.btnEnterActivity.setOnClickListener {

        }
    }

    override fun initData() {
        mLiveData = MutableLiveData()
        mLiveData?.observe(this) {
            val value = mLiveData?.value
            Log.d(TAG, "value = $value")
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_live_data
}