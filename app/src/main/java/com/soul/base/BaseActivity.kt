package com.soul.base

import android.app.ActionBar
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
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

//    protected lateinit var mController: WindowInsetsController

    abstract fun getLayoutId(): Int

    abstract fun initView()

    abstract fun initData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideTitleAndActionBar()
        setContentView(getLayoutId())
        setNavigationBarColor(getNavigationBarColor())
        setStatusBarColor()
        mContext = this
    }

    /**
     * 隐藏标题栏[ActionBar]
     */
    protected open fun hideTitleAndActionBar() {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
    }

    /**
     * 设置导航栏颜色
     */
    protected fun setNavigationBarColor(color: Int) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = color
    }

    protected open fun getNavigationBarColor(): Int = Color.TRANSPARENT

    protected fun setStatusBarColor() {
        val decorView = window.decorView
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.or(View.SYSTEM_UI_FLAG_LAYOUT_STABLE).or(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        decorView.systemUiVisibility = option
        window.statusBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !mUseStatusBarColor) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
    }
}