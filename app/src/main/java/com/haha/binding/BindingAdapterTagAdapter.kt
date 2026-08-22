package com.haha.binding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.haha.hahalearn.R
import com.haha.hahalearn.databinding.ItemBindingAdapterTagBinding

class BindingAdapterTagAdapter : RecyclerView.Adapter<BindingAdapterTagAdapter.TagViewHolder>() {

    private val items = mutableListOf<TagItem>()

    fun submitList(data: List<TagItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = DataBindingUtil.inflate<ItemBindingAdapterTagBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_binding_adapter_tag,
            parent,
            false,
        )
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class TagViewHolder(
        private val binding: ItemBindingAdapterTagBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TagItem) {
            binding.item = item
            binding.executePendingBindings()
        }
    }
}
