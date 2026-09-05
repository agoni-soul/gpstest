package com.haha.splash

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.haha.hahalearn.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * 盖在首页容器之上的开屏层。跳过 / 倒计时结束只揭开首页；点素材才进落地。
 */
class SplashAdOverlay(
    private val activity: Activity,
    private val scope: CoroutineScope,
) {
    interface Callback {
        fun onSkip()
        fun onTimeout()
        fun onAdClick(ad: SplashAd)
    }

    var callback: Callback? = null

    private var root: View? = null
    private var tvSkip: TextView? = null
    private var job: Job? = null
    private var currentAd: SplashAd? = null
    private var left = false
    var remainingSec: Int = 0
        private set

    val isShowing: Boolean
        get() = root?.parent != null && !left

    fun attach(parent: ViewGroup, ad: SplashAd, remainingSec: Int? = null) {
        if (root != null) {
            return
        }
        currentAd = ad
        val view = LayoutInflater.from(activity).inflate(R.layout.overlay_splash_ad, parent, false)
        val ivAd = view.findViewById<ImageView>(R.id.iv_splash_ad)
        tvSkip = view.findViewById(R.id.tv_splash_skip)
        ivAd.setImageResource(ad.imageRes)
        val startSec = remainingSec?.takeIf { it > 0 }
            ?: max(1, (ad.durationMs / 1000L).toInt())
        this.remainingSec = startSec
        tvSkip?.text = activity.getString(R.string.splash_skip_seconds, startSec)
        ivAd.setOnClickListener {
            val ready = currentAd ?: return@setOnClickListener
            if (!markLeft()) {
                return@setOnClickListener
            }
            detach()
            callback?.onAdClick(ready)
        }
        tvSkip?.setOnClickListener {
            if (!markLeft()) {
                return@setOnClickListener
            }
            detach()
            callback?.onSkip()
        }
        parent.addView(
            view,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        root = view
        startCountdown(startSec)
    }

    fun skip() {
        if (!markLeft()) {
            return
        }
        detach()
        callback?.onSkip()
    }

    fun destroy() {
        job?.cancel()
        job = null
        detach()
        callback = null
        currentAd = null
    }

    private fun startCountdown(startSec: Int) {
        job?.cancel()
        job = scope.launch {
            for (sec in startSec downTo 1) {
                if (!isActive || left) {
                    return@launch
                }
                remainingSec = sec
                tvSkip?.text = activity.getString(R.string.splash_skip_seconds, sec)
                delay(1_000)
            }
            if (!markLeft()) {
                return@launch
            }
            detach()
            callback?.onTimeout()
        }
    }

    private fun markLeft(): Boolean {
        if (left) {
            return false
        }
        left = true
        job?.cancel()
        job = null
        return true
    }

    private fun detach() {
        val view = root ?: return
        (view.parent as? ViewGroup)?.removeView(view)
        root = null
        tvSkip = null
    }
}
