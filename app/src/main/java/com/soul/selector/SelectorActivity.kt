package com.soul.selector

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.soul.base.BaseActivity
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-05-09
 *     desc   :
 *     version: 1.0
 */
class SelectorActivity: BaseActivity() {
    private val mTvDS: TextView by lazy {
        findViewById(R.id.tv_ds)
    }
    private val mTvDsEnable: TextView by lazy {
        findViewById(R.id.tv_ds_enable)
    }
    private val mTvDsPress: TextView by lazy {
        findViewById(R.id.tv_ds_press)
    }
    private val mTvCount: TextView by lazy {
        findViewById(R.id.tv_click_count)
    }
    private var mCount = 0
    override fun getLayoutId(): Int = R.layout.activity_selector

    override fun initView() {
        mTvDS.setOnClickListener {
            mCount ++
            mTvCount.text = mCount.toString()
            when (mCount % 6) {
                0 -> {
                    mTvDsEnable.isEnabled = true

                    mTvDsPress.isEnabled = true
                    mTvDsPress.isClickable = true
                    mTvDsPress.isFocusable = true
                    mTvDsPress.requestFocus()
                }
                1 -> {
                    mTvDsEnable.isEnabled = false

                    mTvDsPress.isEnabled = true
                    mTvDsPress.isClickable = false
                    mTvDsPress.isFocusable = true
                    mTvDsPress.requestFocus()
                }
                2 -> {
                    mTvDsPress.isEnabled = true
                    mTvDsPress.isClickable = true
                    mTvDsPress.isFocusable = false
                    mTvDsPress.requestFocus()
                }
                3 -> {
                    mTvDsPress.isEnabled = false
                    mTvDsPress.isClickable = true
                    mTvDsPress.isFocusable = false
                    mTvDsPress.requestFocus()
                }
                4 -> {
                    mTvDsPress.isEnabled = true
                    mTvDsPress.isClickable = false
                    mTvDsPress.isFocusable = false
                    mTvDsPress.requestFocus()
                }
                5 -> {
                    mTvDsPress.isEnabled = false
                    mTvDsPress.isClickable = false
                    mTvDsPress.isFocusable = true
                    mTvDsPress.requestFocus()
                }
            }
        }
    }

    override fun initData() {
    }
}