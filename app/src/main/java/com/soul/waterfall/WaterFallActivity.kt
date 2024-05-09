package com.soul.waterfall

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.soul.gpstest.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.Collator
import java.util.*
import kotlin.random.Random


/**
 *     author : yangzy33
 *     time   : 2024-02-21
 *     desc   :
 *     version: 1.0
 */
class WaterFallActivity : AppCompatActivity() {

    companion object {
        const val BASE_CONTENT = "你了解发号施令旅339ajk,]】；国妮啊后发交电话费盖提亚武器先擦"
    }

    private val tvMap = mutableMapOf<View, Boolean>()

    private val mWaterFallLayout: WaterFallLayout by lazy {
        findViewById(R.id.lt_waterfall)
    }

    private val contentList = mutableListOf<String>()

    private var mPopupWindow: MaskPopupWindow? = null

    private lateinit var mTvParams: LinearLayout.LayoutParams

    private val mMap: MutableMap<String, String> = mutableMapOf()

    private val mViewCount: Button by lazy {
        findViewById(R.id.btn_view_count)
    }

    private val mRv: CustomRecyclerView by lazy {
        findViewById(R.id.rv)
    }

    private lateinit var mCustomViewAdapter: CustomViewAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waterfall)
//        BindViewInject.inject(this, true)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mRv.layoutManager = layoutManager
        mCustomViewAdapter = CustomViewAdapter(this, contentList)
        mRv.adapter = mCustomViewAdapter
        mRv.addItemDecoration(VerticalScrollBarDecoration())

        val etViewCount = findViewById<EditText>(R.id.et_view_count)

        mTvParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        mTvParams.gravity = Gravity.CENTER

        mViewCount.setOnClickListener {
            showPopupWindow()

//            mWaterFallLayout.removeAllViews()
            tvMap.clear()

            contentList.clear()
            val viewCount = etViewCount.text.toString().toIntOrNull() ?: return@setOnClickListener
            val random = Random(0)
            for (i in 0 until viewCount) {
                var startIndex: Int = random.nextInt(BASE_CONTENT.length)
                var endIndex: Int = random.nextInt(BASE_CONTENT.length)
                while (startIndex == endIndex) {
                    startIndex = random.nextInt(BASE_CONTENT.length)
                    endIndex = random.nextInt(BASE_CONTENT.length)
                }
                if (startIndex > endIndex) {
                    val temp = startIndex
                    startIndex = endIndex
                    endIndex = temp
                }
                contentList.add(BASE_CONTENT.substring(startIndex, endIndex))
            }

            contentList.sortWith { o1, o2 ->
                val compare = Collator.getInstance(Locale.CHINA)
                compare.compare(o1, o2)
            }
            mCustomViewAdapter.notifyDataSetChanged()

            for (i in contentList.indices) {
                createSingleTextView(contentList[i], i)
            }

            readJson()

        }
    }

    private fun createSingleTextView(content: String, i: Int = 0) {
        val tv = TextView(this)
        tv.layoutParams = mTvParams
        tv.background = getDrawable(R.drawable.bg_28_gray)
        tv.text = content
        tv.textSize = 22F
        tv.id = tv.hashCode()
        tv.maxLines = 1
        tv.ellipsize = TextUtils.TruncateAt.END
        tv.setTextColor(resources.getColor(R.color.b3ffffff))
        tv.setOnClickListener {
            if (tvMap[tv] == false) {
                tv.background = getDrawable(R.drawable.bg_28_orange)
                tv.setTextColor(resources.getColor(R.color.white))
                tvMap[tv] = true
            } else {
                tv.background = getDrawable(R.drawable.bg_28_gray)
                tv.setTextColor(resources.getColor(R.color.b3ffffff))
                tvMap[tv] = false
            }
        }
        tvMap[tv] = false
        mWaterFallLayout.addView(tv)
    }

    private fun readJson() {
        mMap.clear()
        val jsonStr = ReadJsonUtil.readJson("BusinessHomeDeviceType.json", this) ?: return
        val json = JSONArray(jsonStr)
        try {
            var i = 0
            while (true) {
                val jsonObject: JSONObject
                try {
                    jsonObject = json.getJSONObject(i++)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    break
                }
                val key = jsonObject.get("category").toString()
                val value = jsonObject.get("categoryName").toString()
                mMap[key] = value
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val sb = java.lang.StringBuilder()
        val iter = mMap.keys.iterator()
        while (iter.hasNext()) {
            val key = iter.next()
            sb.append("key = $key, value = ${mMap[key]}\n")
        }
        Log.d(this.javaClass.simpleName, sb.toString())
    }

    private fun showPopupWindow() {
        if (mPopupWindow == null) {
            mPopupWindow = MaskPopupWindow(this)
            mPopupWindow?.animationStyle = R.style.business_home_add_popup_anim
        }
        if (mPopupWindow?.isShowing == true) {
            mPopupWindow?.dismiss()
            return
        }

        mViewCount.postDelayed({
            mPopupWindow?.showAsDropDown(
                mViewCount as View,
                0,
                32,
                Gravity.START
            )
        }, 100)
    }
}