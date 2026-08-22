package com.haha.binding

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter

object BindingAdapters {

    @JvmStatic
    @BindingAdapter("visibleGone")
    fun setVisibleGone(view: View, visible: Boolean?) {
        view.visibility = if (visible == true) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("labelText")
    fun setLabelText(textView: TextView, pair: Pair<String, String>?) {
        if (pair == null) {
            textView.text = ""
            return
        }
        textView.text = "${pair.first}：${pair.second}"
    }

    @JvmStatic
    @BindingAdapter("tagColorRes")
    fun setTagColor(textView: TextView, colorRes: Int?) {
        if (colorRes == null || colorRes == 0) return
        val color = ContextCompat.getColor(textView.context, colorRes)
        textView.setBackgroundColor(color)
    }
}
