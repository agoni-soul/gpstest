package com.soul.waterfall

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-03-11
 *     desc   :
 *     version: 1.0
 */
class CustomViewAdapter(private val mContext: Context, data: MutableList<String>?): RecyclerView.Adapter<CustomViewAdapter.ViewHolder>() {

    private var mData: MutableList<String>? = data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.adapter_item_text, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mData?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemText.text = mData?.get(position) ?: "你好"
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var itemText: TextView

        init {
            itemText = itemView.findViewById(R.id.tv_item)
        }
    }
}