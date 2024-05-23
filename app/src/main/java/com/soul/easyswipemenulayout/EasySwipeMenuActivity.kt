package com.soul.easyswipemenulayout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.soul.base.BaseMvvmActivity
import com.soul.base.BaseViewModel
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityEasySwipeMenuBinding


/**
 *     author : yangzy33
 *     time   : 2024-05-23
 *     desc   :
 *     version: 1.0
 */
class EasySwipeMenuActivity : BaseMvvmActivity<ActivityEasySwipeMenuBinding, BaseViewModel>() {
    private val ll: EasySwipeMenuLayout? = null

    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_easy_swipe_menu

    override fun initView() {
        recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        myAdapter = MyAdapter(this)
        recyclerView?.setAdapter(myAdapter!!)
        inflater = layoutInflater
    }

    override fun initData() {
        listData = ArrayList<String>()
        for (i in 0..19) {
            listData?.add("index is =$i")
        }
        myAdapter?.updateData(listData!!)
        myAdapter?.notifyDataSetChanged()
    }


    private var recyclerView: RecyclerView? = null
    private var myAdapter: MyAdapter? = null
    private var listData: MutableList<String>? = null
    private var inflater: LayoutInflater? = null

    inner class MyAdapter(private val context: Context) : RecyclerView.Adapter<MyHolder>() {
        private val listData: MutableList<String> = mutableListOf()

        fun updateData(listData: MutableList<String>) {
            this.listData.clear()
            this.listData.addAll(listData)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_item_rv_swipemenu, null, false)
            return MyHolder(view)
        }

        override fun getItemCount(): Int = listData.size

        override fun onBindViewHolder(helper: MyHolder, position: Int) {
            helper.rightMenu2.setOnClickListener {
                Toast.makeText(context, "收藏", Toast.LENGTH_SHORT).show()
                val easySwipeMenuLayout: EasySwipeMenuLayout = helper.es
                easySwipeMenuLayout.resetStatus()
            }
            helper.content.setOnClickListener {
                Toast.makeText(
                    context, "setOnClickListener", Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

}

class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val rightMenu2: TextView
    val content: LinearLayout
    val es: EasySwipeMenuLayout

    init {
        es = itemView.findViewById(R.id.es)
        rightMenu2 = itemView.findViewById(R.id.right_menu_2)
        content = itemView.findViewById(R.id.content)
    }
}