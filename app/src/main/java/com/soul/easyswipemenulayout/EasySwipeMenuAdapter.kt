package com.soul.easyswipemenulayout

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.soul.gpstest.R
import com.soul.log.DOFLogUtil

class EasySwipeMenuAdapter(private val context: Context) : RecyclerView.Adapter<EasySwipeMenuHolder>() {
    private val TAG = javaClass.simpleName
    private val listData: MutableList<String> = mutableListOf()

    fun updateData(listData: MutableList<String>) {
        this.listData.clear()
        this.listData.addAll(listData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EasySwipeMenuHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_item_rv_swipemenu, parent, false)
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