package com.soul.scene

import android.os.Bundle
import android.transition.*
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.soul.base.BaseActivity
import com.soul.gpstest.R


open class CustomSceneSecondActivity : BaseActivity() {
    private val sceneRoot: ConstraintLayout by lazy {
        findViewById(R.id.sceneRoot)
    }
    private val vSquare: View by lazy {
        findViewById(R.id.vSquare)
    }

    override fun getLayoutId(): Int = R.layout.activity_custom_scene_second

    override fun initView() {
        vSquare.setOnClickListener {
            TransitionManager.beginDelayedTransition(sceneRoot, TransitionSet().apply {
                addTransition(ChangeImageTransform())
                addTransition(ChangeBounds())
                addTransition(Fade(Fade.MODE_IN))
            }.also {
                window.sharedElementEnterTransition = it
            })

            vSquare.layoutParams.apply {
                width = dp2px(200)
                height = dp2px(200)
            }.also {
                vSquare.layoutParams = it
            }
        }
    }

    override fun initData() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_scene_second)

    }

    /**
     * 将dp转换成对用的px
     */
    private fun dp2px(dp: Int): Int = TypedValue
        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()

    /**
     * 将sp转换成对用的px
     */
    protected fun sp2px(sp: Int): Int = TypedValue
        .applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), resources.displayMetrics).toInt()
}