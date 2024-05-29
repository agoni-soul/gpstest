package com.soul.easyswipemenulayout

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soul.gpstest.R

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