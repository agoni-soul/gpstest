package com.soul.base

import android.app.ActionBar
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
        setStatusBarColor(getStatusBarColor())
        setStatusBarTextColor(isBlackStatusText())
        setNavigationBarColor(getNavigationBarColor())
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
     * 设置导航栏背景颜色
     *
     * @param 背景颜色
     */
    private fun setNavigationBarColor(color: Int) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = color
    }

    protected open fun getNavigationBarColor(): Int = Color.TRANSPARENT

    /**
     * 设置状态栏背景颜色
     *
     * @param color 背景颜色
     */
    private fun setStatusBarColor(color: Int) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color

//        val decorView = window.decorView
//        val option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE.or(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
//        decorView.systemUiVisibility = option
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !mUseStatusBarColor) {
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
//        }
    }

    protected open fun getStatusBarColor(): Int = Color.TRANSPARENT

    /**
     * 设置状态栏字体颜色
     *
     * @param isBlack 字体是否为黑色
     */
    private fun setStatusBarTextColor(isBlack: Boolean) {
        if (isBlack) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    protected open fun isBlackStatusText(): Boolean = true
}