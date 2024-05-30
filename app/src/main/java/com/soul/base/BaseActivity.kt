package com.soul.base

import android.R
import android.annotation.TargetApi
import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import com.soul.log.DOFLogUtil


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

    /**
     * 隐藏标题栏[ActionBar]
     */
    protected open fun hideTitleAndActionBar() {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
    }

    protected open fun getNavigationBarColor(): Int = Color.TRANSPARENT

    protected open fun getStatusBarColor(): Int = Color.TRANSPARENT

    protected open fun isShowNavigation(): Boolean = true

    protected open fun isBlackStatusText(): Boolean = true

    protected open fun isShowStatus(): Boolean = true

    protected open fun getRootViewId(): Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideTitleAndActionBar()
        if (!isShowStatus()) {
//            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        setContentView(getLayoutId())
        setStatusBarColor(getStatusBarColor())
        setNavigationBarColor(getNavigationBarColor())
        handleNavigationVAndStatusVisibility()
        mContext = this
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

    /**
     * 设置状态栏背景颜色
     *
     * @param color 背景颜色
     */
    private fun setStatusBarColor(color: Int) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
    }

    private fun handleNavigationVAndStatusVisibility() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            handleAdvancedSystemNavigationAndStatus()
//        } else {
            handleNormalSystemNavigationAndStatus()
//        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun handleAdvancedSystemNavigationAndStatus() {
        if (window.insetsController == null) {
            handleNormalSystemNavigationAndStatus()
            return
        }
        DOFLogUtil.d(TAG, "handleAdvancedSystemNavigationAndStatus")
        val isShowNavigation = isShowNavigation()
        val isShowStatus = isShowStatus()
        val isBlackStatusText = isBlackStatusText()
        window.insetsController!!.apply {
            if (isShowNavigation) {
                show(WindowInsetsCompat.Type.navigationBars())
            } else {
                hide(WindowInsetsCompat.Type.navigationBars())
            }
            if (isShowStatus) {
                show(WindowInsetsCompat.Type.statusBars())
            } else {
                hide(WindowInsetsCompat.Type.statusBars())
            }
            if (isBlackStatusText) {
                setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
            }
        }
    }

    private fun handleNormalSystemNavigationAndStatus() {
        DOFLogUtil.d(TAG, "handleNormalSystemNavigationAndStatus")
        val isShowNavigation = isShowNavigation()
        val isShowStatus = isShowStatus()
        val isBlackStatusText = isBlackStatusText()
        var uiOption = View.SYSTEM_UI_FLAG_VISIBLE
        DOFLogUtil.d(TAG, "uiOption = $uiOption")
        // 设置隐藏导航栏
        if (!isShowNavigation) {
            uiOption = uiOption.or(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                .or(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) // 增加沉淀式体验, 即底部上划可展示导航栏，会自动消失
            DOFLogUtil.d(TAG, "uiOption = $uiOption, isShowNavigation = $isShowNavigation")
        }
        if (isBlackStatusText) {
            uiOption = uiOption.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                .or(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            DOFLogUtil.d(TAG, "uiOption = $uiOption, isBlackStatusText = $isBlackStatusText")
        }
        // 设置状态栏字体颜色为黑色
        if (isShowStatus) {
        } else {
            uiOption = uiOption.or(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                .or(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            window.statusBarColor = Color.TRANSPARENT
            if (getRootViewId() != 0) {
                val rootView = findViewById<ViewGroup>(getRootViewId())
                if (rootView != null) {
                    rootView.fitsSystemWindows = true
                    // 留出高度 setFitsSystemWindows  true代表会调整布局，会把状态栏的高度留出来
                    val contentView: View = rootView.getChildAt(0)
                    contentView.fitsSystemWindows = true
                    // 在原来的位置上添加一个状态栏
                    val statusBarView = createStatusBarView(this)
                    rootView.addView(statusBarView, 0)
                }
            }
            DOFLogUtil.d(TAG, "uiOption = $uiOption, isShowStatus = $isShowStatus")
        }
        DOFLogUtil.d(TAG, "uiOption = $uiOption")
        window.decorView.systemUiVisibility = uiOption

        // 这种方法顶部会有黑边，
//        if (isShowStatus) {
//            val lp = window.attributes
//            lp.flags = lp.flags.and(WindowManager.LayoutParams.FLAG_FULLSCREEN.inv())
//            window.attributes = lp
//        }
    }

    /**
     * 创建一个需要填充statusBarView
     */
    private fun createStatusBarView(activity: Activity): View {
        val statusBarView = View(activity)
        val statusBarParams: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity)
        )
        statusBarView.layoutParams = statusBarParams
        return statusBarView
    }

    /**
     * 获取状态栏的高度
     */
    private fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        DOFLogUtil.d(TAG, "getStatusBarHeight = $result")
        return result
    }
}