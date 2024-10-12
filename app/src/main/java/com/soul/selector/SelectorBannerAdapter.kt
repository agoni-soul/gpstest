package com.soul.selector

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.soul.gpstest.R
import com.youth.banner.adapter.BannerAdapter

/**
 *
 * @author : haha
 * @date   : 2024-10-11
 * @desc   : 使用Banner进行展示
 *
 */
class SelectorBannerAdapter(data: MutableList<String>): BannerAdapter<String, SelectorBannerAdapter.SelectorViewHolder>(data) {

    override fun onBindView(holder: SelectorViewHolder?, data: String?, position: Int, size: Int) {
        holder ?: return
        holder.view.background =  AppCompatResources.getDrawable(holder.itemView.context, R.color.gray_A0A0A0)
    }

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): SelectorViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.adapter_item_banner, parent, false)
        return SelectorViewHolder(view)
    }

    inner class SelectorViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val view: View = itemView.findViewById(R.id.view_banner_item)
    }
}