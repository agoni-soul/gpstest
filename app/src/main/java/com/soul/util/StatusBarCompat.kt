package com.soul.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowInsetsCompat
import com.soul.log.DOFLogUtil


/**
 *     author : yangzy33
 *     time   : 2024-08-12
 *     desc   :
 *     version: 1.0
 */
object StatusBarCompat {
    val TAG = javaClass.simpleName

    fun handleNavigationVAndStatusVisibility(
        activity: Activity,
        isShowNavigation: Boolean,
        isShowStatus: Boolean,
        isBlackStatusText: Boolean,
        rootViewId: Int,
        statusBarColor: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            handleAdvancedSystemNavigationAndStatus(
                activity,
                isShowNavigation,
                isShowStatus,
                isBlackStatusText,
                rootViewId,
                statusBarColor
            )
        } else {
            handleNormalSystemNavigationAndStatus(
                activity,
                isShowNavigation,
                isShowStatus,
                isBlackStatusText,
                rootViewId,
                statusBarColor
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun handleAdvancedSystemNavigationAndStatus(
        activity: Activity,
        isShowNavigation: Boolean,
        isShowStatus: Boolean,
        isBlackStatusText: Boolean,
        rootViewId: Int,
        statusBarColor: Int
    ) {
        val window = activity.window
        if (window.insetsController == null) {
            handleNormalSystemNavigationAndStatus(
                activity,
                isShowNavigation,
                isShowStatus,
                isBlackStatusText,
                rootViewId,
                statusBarColor
            )
            return
        }
        DOFLogUtil.d(TAG, "handleAdvancedSystemNavigationAndStatus")
        window.insetsController!!.apply {
            // TODO 隐藏导航栏，上滑仍会会显示，后续再研究
            if (isShowNavigation) {
                show(WindowInsetsCompat.Type.navigationBars())
            } else {
                hide(WindowInsetsCompat.Type.navigationBars())
            }
            if (isShowStatus) {
                show(WindowInsetsCompat.Type.statusBars())
            } else {
                // TODO 顶部有黑边，暂时不生效，后续再研究
//                hide(WindowInsetsCompat.Type.statusBars())
//                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                val uiOption =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.or(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                window.decorView.systemUiVisibility = uiOption
                Color.TRANSPARENT
                window.statusBarColor = window.context.resources.getColor(statusBarColor)
                addStatusBarView(activity, rootViewId, statusBarColor)
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

    private fun handleNormalSystemNavigationAndStatus(
        activity: Activity,
        isShowNavigation: Boolean,
        isShowStatus: Boolean,
        isBlackStatusText: Boolean,
        rootViewId: Int,
        statusBarColor: Int
    ) {
        DOFLogUtil.d(TAG, "handleNormalSystemNavigationAndStatus")
        var uiOption = View.SYSTEM_UI_FLAG_VISIBLE
        val window = activity.window
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
        if (!isShowStatus) {
            uiOption = uiOption.or(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                .or(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            Color.TRANSPARENT
            window.statusBarColor = window.context.resources.getColor(statusBarColor)
            addStatusBarView(activity, rootViewId, statusBarColor)
            DOFLogUtil.d(TAG, "uiOption = $uiOption, isShowStatus = $isShowStatus")
        }
        DOFLogUtil.d(TAG, "uiOption = $uiOption")
        window.decorView.systemUiVisibility = uiOption
    }

    /**
     * 把创建的StatusBar添加到布局中
     */
    private fun addStatusBarView(activity: Activity, rootViewId: Int, statusBarColor: Int) {
        if (rootViewId != 0) {
            val rootView = activity.findViewById<ViewGroup>(rootViewId)
            DOFLogUtil.d(TAG, "rootView = $rootView")
            if (rootView != null) {
                rootView.fitsSystemWindows = true
                // 在原来的位置上添加一个状态栏
                val statusBarView = createStatusBarView(activity, statusBarColor)
                statusBarView.fitsSystemWindows = true
                DOFLogUtil.d(
                    TAG,
                    "statusBarView = $statusBarView, height = ${statusBarView.height}"
                )
                rootView.addView(statusBarView, 0)
                rootView.requestLayout()
            }
        }
    }

    /**
     * 创建一个需要填充statusBarView
     */
    private fun createStatusBarView(activity: Activity, statusBarColor: Int): View {
        val statusBarView = View(activity)
        statusBarView.background =
            ResourcesCompat.getDrawable(activity.resources, statusBarColor, null)
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