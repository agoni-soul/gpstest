package com.soul.easyswipemenulayout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.soul.base.BaseMvvmActivity
import com.soul.base.BaseViewModel
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityEasySwipeMenuBinding
import com.soul.log.DOFLogUtil


/**
 *     author : yangzy33
 *     time   : 2024-05-23
 *     desc   :
 *     version: 1.0
 */
class EasySwipeMenuActivity : BaseMvvmActivity<ActivityEasySwipeMenuBinding, BaseViewModel>() {
    private lateinit var easySwipeMenuAdapter: EasySwipeMenuAdapter
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private val listData: MutableList<String> by lazy {
        mutableListOf()
    }

    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_easy_swipe_menu

    override fun initView() {
        val wm: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        DOFLogUtil.d(TAG, "match_parent = ${wm.defaultDisplay.width}")
        mLinearLayoutManager = LinearLayoutManager(this)
        mViewDataBinding?.recyclerview?.layoutManager = mLinearLayoutManager
        easySwipeMenuAdapter = EasySwipeMenuAdapter(this)
        mViewDataBinding?.recyclerview?.adapter = easySwipeMenuAdapter
        mViewDataBinding?.recyclerview?.viewTreeObserver?.addOnGlobalLayoutListener(listener)
    }

    private val listener: OnGlobalLayoutListener by lazy {
        OnGlobalLayoutListener {
            val width = mViewDataBinding?.recyclerview?.width ?: 0
            val itemWidth = mLinearLayoutManager.width
            DOFLogUtil.d(TAG, "viewTreeObserver: width = $width, itemWidth = $itemWidth")
            mViewDataBinding?.recyclerview?.viewTreeObserver?.removeOnGlobalLayoutListener(listener)
        }
    }

    override fun initData() {
        for (i in 0..19) {
            listData.add("index is =$i")
        }
        easySwipeMenuAdapter.updateData(listData)
        easySwipeMenuAdapter.notifyDataSetChanged()
    }
}

class EasySwipeMenuAdapter(private val context: Context) : RecyclerView.Adapter<EasySwipeMenuHolder>() {
    private val TAG = javaClass.simpleName
    private val listData: MutableList<String> = mutableListOf()

    fun updateData(listData: MutableList<String>) {
        this.listData.clear()
        this.listData.addAll(listData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EasySwipeMenuHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_item_rv_swipemenu, null, false)
        return EasySwipeMenuHolder(view)
    }

    override fun getItemCount(): Int = listData.size

    override fun onBindViewHolder(helper: EasySwipeMenuHolder, position: Int) {
        helper.rightMenu2.setOnClickListener {
            Toast.makeText(context, "收藏", Toast.LENGTH_SHORT).show()
            val easySwipeMenuLayout: EasySwipeMenuLayout = helper.esSwipe
            easySwipeMenuLayout.resetStatus()
        }
        DOFLogUtil.d(TAG, "width = ${helper.content.width}")
        helper.content.setOnClickListener {
            Toast.makeText(
                context, "setOnClickListener", Toast.LENGTH_SHORT
            ).show()
        }
    }

}

class EasySwipeMenuHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val rightMenu2: TextView
    val content: TextView
    val esSwipe: EasySwipeMenuLayout

    init {
        esSwipe = itemView.findViewById(R.id.es_swipe)
        rightMenu2 = itemView.findViewById(R.id.right_menu_2)
        content = itemView.findViewById(R.id.content)
    }
}