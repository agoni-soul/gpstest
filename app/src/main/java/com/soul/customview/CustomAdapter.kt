package com.soul.customview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-03-13
 *     desc   :
 *     version: 1.0
 */
class CustomAdapter(private val mContext: Context, data: MutableList<String>?):
    RecyclerView.Adapter<CustomAdapter.CustomViewHolder>() {
    private val mData: MutableList<String> = mutableListOf()

    init {
        updateData(data)
    }

    fun updateData(data: MutableList<String>?) {
        data?.let {
            mData.clear()
            mData.addAll(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val content = LayoutInflater.from(mContext).inflate(R.layout.adapter_item_custom, parent, false)
        return CustomViewHolder(content)
    }

    override fun getItemCount(): Int = mData.size

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.tvName.text = position.toString()
        holder.tvContent.text = mData[position]
    }


    inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName = view.findViewById<TextView>(R.id.tv_name)
        val tvContent = view.findViewById<TextView>(R.id.tv_content)
    }
}