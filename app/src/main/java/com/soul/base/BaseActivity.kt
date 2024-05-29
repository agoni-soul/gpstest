package com.soul.base

import android.app.ActionBar
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-05-11
 *     desc   :
 *     version: 1.0
 */
abstract class BaseActivity : AppCompatActivity() {
    protected open val TAG = javaClass.simpleName

    protected lateinit var mContext: Context

    protected var mUseThemeStatusBarColor = false

    protected var mUseStatusBarColor = true

    abstract fun getLayoutId(): Int

    abstract fun initView()

    abstract fun initData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideTitleAndActionBar()
        setContentView(getLayoutId())
        setNavigationBarColor()
        setStatusBarColor()
        mContext = this
    }

    /**
     * 隐藏导航栏[ActionBar]
     */
    protected open fun hideTitleAndActionBar() {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
    }

    /**
     * 设置导航栏颜色
     */
    protected fun setNavigationBarColor() {
        window.navigationBarColor = Color.TRANSPARENT
    }

    protected fun setStatusBarColor() {
        val decorView = window.decorView
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.or(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        decorView.systemUiVisibility = option
        window.statusBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !mUseStatusBarColor) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
    }
}