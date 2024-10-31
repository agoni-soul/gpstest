package com.soul.dynamicTextView

import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.soul.base.BaseMvvmActivity
import com.soul.base.BaseViewModel
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityDynamicTextviewBinding
import com.soul.util.DpOrSpToPxTransfer
import java.security.spec.EllipticCurve

/**
 *
 * @author : haha
 * @date   : 2024-10-31
 * @desc   : 动态TextView展示
 *
 */
class DynamicTextViewActivity: BaseMvvmActivity<ActivityDynamicTextviewBinding, BaseViewModel>() {
    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun initView() {
        mViewDataBinding.apply {
            btnUpdate.setOnClickListener {
                val value = etValue.text.toString()
                if (value.isEmpty()) {
                    Toast.makeText(this@DynamicTextViewActivity, "请输入数值", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val unit = etUnit.text.toString()
                if (unit.isEmpty()) {
                    Toast.makeText(this@DynamicTextViewActivity, "请输入单位", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                tvUnit.text = unit
                val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                tvUnit.measure(widthSpec, heightSpec)
                val width = tvUnit.measuredWidth
                Log.d(TAG, "setOnClickListener: width = ${llValue.width}, unitWidth = $width")
                calculateTextViewWidth(llValue, tvValue, value, llValue.width - width)
//                tvValue.text = value
            }
        }
    }

    private fun calculateTextViewWidth(ll: LinearLayout, tv: TextView, value: String, leaveLength: Int): Int {
        tv.text = value
        tv.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        tv.measure(widthSpec, heightSpec)
        val width = tv.measuredWidth
        Log.d(TAG, "calculateTextViewWidth: width = $width")
        ll.gravity = Gravity.CENTER_HORIZONTAL.or(Gravity.BOTTOM)
        return if (width < leaveLength) {
//            tv.ellipsize = TextUtils.TruncateAt.END
//            tv.setLines(1)
//            tv.setTextColor(resources.getColor(R.color.white))
//            tv.textSize = DpOrSpToPxTransfer.px2sp(this, 80f)
            width
        } else {
            ll.gravity = Gravity.CENTER_HORIZONTAL.or(Gravity.BOTTOM)
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            params.gravity = Gravity.END
            tv.layoutParams = params
            tv.ellipsize = TextUtils.TruncateAt.END
//            tv.setLines(1)
//            tv.setTextColor(resources.getColor(R.color.white))
//            tv.textSize = DpOrSpToPxTransfer.px2sp(this, 80f)
            leaveLength
        }
    }

    override fun initData() {
    }

    override fun getLayoutId(): Int = R.layout.activity_dynamic_textview

}