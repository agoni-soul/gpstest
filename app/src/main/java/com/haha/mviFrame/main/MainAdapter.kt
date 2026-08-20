package com.haha.mviFrame.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haha.gpstest.R

/**
 * @auther: haha
 * @Date:   2025/12/7
 * @Detail:
 */
class MainAdapter(
    private val users: ArrayList<User>
) : RecyclerView.Adapter<MainAdapter.DataViewHolder>() {

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(user: User) {
            itemView.findViewById<TextView>(R.id.textViewUserName).text = user.name
            itemView.findViewById<TextView>(R.id.textViewUserEmail).text = user.email
            val imageViewAvatar = itemView.findViewById<ImageView>(R.id.imageViewAvatar)
            Glide.with(imageViewAvatar.context)
                .load(user.avator)
                .into(imageViewAvatar)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DataViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_mvi_layout, parent,
                false
            )
        )

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) =
        holder.bind(users[position])

    fun submitUsers(list: List<User>) {
        users.clear()
        users.addAll(list)
        notifyDataSetChanged()
    }

}


