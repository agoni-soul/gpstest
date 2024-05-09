package com.soul.selector

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-05-09
 *     desc   :
 *     version: 1.0
 */
class SelectorActivity: AppCompatActivity() {
    private val mTvDS: TextView by lazy {
        findViewById(R.id.tv_ds_test_content)
    }
    private val mTvCount: TextView by lazy {
        findViewById(R.id.tv_click_count)
    }
    private var mCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selector)

        mTvDS.setOnClickListener {
            mCount ++
            mTvCount.text = mCount.toString()
            when (mCount % 6) {
                0 -> {
                    mTvDS.isEnabled = true
                }
                1 -> {

                }
            }
        }
    }
}