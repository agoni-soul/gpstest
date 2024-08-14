package com.soul.bluetooth

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.soul.base.BaseFragment
import com.soul.base.BaseMvvmActivity
import com.soul.base.BaseMvvmFragment
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

    private val mScanFragment: BaseFragment by lazy {
        BleScanFragment()
    }

    private var isInitScanFragment = false

    private val mBoundFragment: BaseFragment by lazy {
        BleBoundFragment()
    }

    private var isInitBoundFragment = false

    private lateinit var mLastFragment: BaseFragment

    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_bluetooth

    override fun initView() {
        mViewDataBinding.tvBleScan.setOnClickListener {
            switchScanFragment()
        }
        mViewDataBinding.tvBleBound.setOnClickListener {
            switchBoundFragment()
        }
        switchScanFragment()
    }

    private fun switchScanFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        if (!isInitScanFragment) {
            transaction.add(R.id.fl_container, mScanFragment)
            isInitScanFragment = true
        } else if (mLastFragment != mScanFragment) {
            transaction.hide(mLastFragment)
            transaction.show(mScanFragment)
        }
        transaction.addToBackStack(null)
        transaction.commit()
        mLastFragment = mScanFragment
    }

    private fun switchBoundFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        if (!isInitBoundFragment) {
            transaction.add(R.id.fl_container, mBoundFragment)
            isInitBoundFragment = true
        } else if (mLastFragment != mBoundFragment) {
            transaction.hide(mLastFragment)
            transaction.show(mBoundFragment)
        }
        transaction.addToBackStack(null)
        transaction.commit()
        mLastFragment = mBoundFragment
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