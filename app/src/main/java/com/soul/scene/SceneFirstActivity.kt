package com.soul.scene

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.soul.gpstest.R

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/10/27
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class SceneFirstActivity : AppCompatActivity() {
    private var activity: Activity? = null

    private val ivImage: ImageView by lazy {
        findViewById(R.id.ivFirstImage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSceneAnimation()

        setContentView(R.layout.activity_scene_first)
        activity = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ivImage.transitionName = "activityTransform"
        }
        ivImage.setOnClickListener { v: View? ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //判断Android版本
//                val bundle =
//                    ActivityOptions.makeSceneTransitionAnimation(activity, ivImage,"activityTransform")
//                        .toBundle()
                val bundle = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
                startActivity(Intent(activity, SceneSecondActivity::class.java), bundle)
            } else {
                startActivity(Intent(activity, SceneSecondActivity::class.java))
            }
        }
    }

    private fun setSceneAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            window.allowEnterTransitionOverlap = false
            Slide().apply {
                slideEdge = Gravity.BOTTOM
                duration = 300
                excludeTarget(android.R.id.statusBarBackground, true)
                excludeTarget(android.R.id.navigationBarBackground, true)
            }.also {
                window.exitTransition = it
                window.enterTransition = it
                window.reenterTransition = it
            }
        }

    }
}