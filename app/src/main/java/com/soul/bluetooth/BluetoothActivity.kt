package com.soul.bluetooth

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.soul.base.BaseFragment
import com.soul.base.BaseMvvmActivity
import com.soul.base.BaseViewModel
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityBluetoothBinding


/**
 *     author : yangzy33
 *     time   : 2024-07-04
 *     version: 1.0
 */
class BluetoothActivity : BaseMvvmActivity<ActivityBluetoothBinding, BaseViewModel>() {

    companion object {
        const val REQUEST_CODE_PERMISSION = 101
        const val REQUEST_CODE_BLUETOOTH_DISCOVERABLE = 102
    }

    private lateinit var mBleViewPager: ViewPager2
    private lateinit var mTabLayout: TabLayout

    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_bluetooth

    override fun initView() {
        mBleViewPager = mViewDataBinding.bleViewPager
        mTabLayout = mViewDataBinding.bleTabLayout
        val fragmentList = mutableListOf<BaseFragment>()
        fragmentList.add(BleScanFragment())
        fragmentList.add(BleScanFragment())
//        fragmentList.add(BleBoundFragment())
        val pagerAdapter = ViewPagerFragmentStateAdapter(supportFragmentManager, this)
        mBleViewPager.adapter = pagerAdapter
        mTabLayout.apply {
            setupWithViewPager(mViewDataBinding.bleViewPager)
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.apply {
                        val selectTab = text.toString().trim()
                        val spannableStr = SpannableString(selectTab)
                        val styleSpan = StyleSpan(Typeface.BOLD)
                        spannableStr.setSpan(
                            styleSpan,
                            0,
                            selectTab.length,
                            Spanned.SPAN_INCLUSIVE_INCLUSIVE
                        )
                        tab.text = spannableStr
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    tab?.apply {
                        val unSelectTab = text.toString().trim()
                        val spannableStr = SpannableString(unSelectTab)
                        val styleSpan = StyleSpan(Typeface.NORMAL)
                        spannableStr.setSpan(
                            styleSpan,
                            0,
                            unSelectTab.length,
                            Spanned.SPAN_INCLUSIVE_INCLUSIVE
                        )
                        tab.text = spannableStr
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
        }

    }

    override fun initData() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_BLUETOOTH_DISCOVERABLE) {
            Log.d(TAG, "resultCode = ${resultCode}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION -> {
                for (i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d(
                            TAG,
                            "onRequestPermissionsResult: permission = ${permissions[i]}, grantValue = ${grantResults[i]}"
                        )
                    }
                }
            }
        }
    }
}