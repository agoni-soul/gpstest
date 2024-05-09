package com.soul.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.transition.Fade
import android.transition.Slide
import android.util.Log
import android.view.View
import android.view.animation.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.soul.bean.DeviceInfo
import com.soul.gpstest.R


/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/10/08
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class AnimationActivity: AppCompatActivity() {
    private lateinit var mIvLoading: ImageView

    private lateinit var mTvTitle: TextView

    private lateinit var mTvSubTitle: TextView

    private lateinit var mTvUnfoundTips: TextView

    private lateinit var mTvFindTips: TextView

    private lateinit var mRvSubDevice: RecyclerView

    private lateinit var mBtNext: Button

    private var mSubDeviceAdapter: SearchSubDeviceAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation)
//        window.enterTransition = Slide()

        mIvLoading = findViewById(R.id.iv_loading)
        mTvTitle = findViewById(R.id.tv_title)
        mTvSubTitle = findViewById(R.id.tv_subtitle)
        mTvUnfoundTips = findViewById(R.id.tv_unfound_tips)
        mTvFindTips = findViewById(R.id.tv_find_tips)
        mRvSubDevice = findViewById(R.id.rv_subdevice)
        mBtNext = findViewById(R.id.bt_next)

        val animation = AnimationUtils.loadAnimation(this, R.anim.rotateanim)
        mIvLoading.startAnimation(animation)
        mSubDeviceAdapter = SearchSubDeviceAdapter(this, mutableListOf(),
            object : SearchSubDeviceAdapter.OnItemClickCallback {
                override fun onItemClick(isBindSuccess: Boolean) {
                    // TODO 后续补充
                    if (isBindSuccess) {
                        Toast.makeText(this@AnimationActivity, "绑定成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@AnimationActivity, "绑定失败", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        mRvSubDevice.adapter = mSubDeviceAdapter
        mRvSubDevice.layoutManager = LinearLayoutManager(this)

        mRvSubDevice.postDelayed(
            {
                mIvLoading.animation.cancel()
                val animationLoading = AnimationUtils.loadAnimation(this, R.anim.move_reduce_anim)
                mIvLoading.startAnimation(animationLoading)

                val animationText = AnimationUtils.loadAnimation(this, R.anim.move_reduce_text_anim)
                mTvTitle.startAnimation(animationText)

                mRvSubDevice.visibility = View.VISIBLE
        }, 2000)

        mBtNext.postDelayed(
            {
//                mIvLoading.animation.cancel()
//                val animationLoading = AnimationUtils.loadAnimation(this, R.anim.move_reduce1_anim)
//                mIvLoading.startAnimation(animationLoading)

                addData()
                findSubDevice()
            }, 3000)
    }

    private fun addData() {
        val mutableList = mutableListOf<DeviceInfo>()
        mutableList.add(DeviceInfo("12", "12345678", "你好", false))
        mutableList.add(DeviceInfo("12", "12345678", "你好", true))
        mutableList.add(DeviceInfo("12", "12345678", "你好你好你好", false))
        mutableList.add(DeviceInfo("12", "12345678", "你好nihainihao", true))
        mutableList.add(DeviceInfo("12", "12345678", "你好", false))
        mutableList.add(DeviceInfo("12", "12345678", "你好nihainihao", true))
        mutableList.add(DeviceInfo("12", "12345678", "你好", false))
        mutableList.add(DeviceInfo("12", "12345678", "你好nihainihao", true))
        mutableList.add(DeviceInfo("12", "12345678", "你好", false))
        mutableList.add(DeviceInfo("12", "12345678", "你好nihainihao", true))
        mutableList.add(DeviceInfo("12", "12345678", "你好", false))
        mutableList.sortBy {
            !it.isOnline
        }
        mSubDeviceAdapter?.addNewData(mutableList)
        LogUtils.d("hahhahah", GsonUtils.toJson(mSubDeviceAdapter?.mData))
    }

    private fun findSubDevice() {
        mTvFindTips.visibility = View.VISIBLE
        mRvSubDevice.visibility = View.VISIBLE
        mBtNext.visibility = View.VISIBLE

        mTvUnfoundTips.visibility = View.GONE
        mTvSubTitle.visibility = View.GONE
    }
}