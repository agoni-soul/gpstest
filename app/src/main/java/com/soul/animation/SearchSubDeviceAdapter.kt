package com.soul.animation

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.soul.bean.DeviceInfo
import com.soul.gpstest.R

/**
 *     author : yangzy33
 *     time   : 2023/6/9
 *     desc   :
 *     version: 1.0
 */
class SearchSubDeviceAdapter(context: Context, data: MutableList<DeviceInfo>? = null):
    RecyclerView.Adapter<SearchSubDeviceAdapter.ViewHolder>() {

    constructor(context: Context, data: MutableList<DeviceInfo>?, callback: OnItemClickCallback): this(context, data) {
        mCallback = callback
    }

    private val mSelectRbList = mutableListOf<Boolean>()

    private var mContext: Context? = context

    val mData: MutableList<DeviceInfo>? = data

    private var mCallback: OnItemClickCallback? = null

    interface OnItemClickCallback {
        fun onItemClick(isBindSuccess: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = LayoutInflater.from(parent.context).inflate(R.layout.config_item_scan_common, parent, false)
        return ViewHolder(holder)
    }

    override fun getItemCount(): Int = mData?.size ?: 0

    private var selectPosition: Int = -1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mData?.get(position)?.apply {
            holder.mIvIcon.setImageResource(R.drawable.net_ic_wlan)
            holder.mTvTitle.text = name
            if (isOnline) {
                holder.mTvSubTitle.text = mContext?.getString(R.string.config_bind_success)
                val drawable = mContext?.resources?.getDrawable(R.drawable.config_command_ic_sucess_s, null)
                val value = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18f, mContext?.resources?.displayMetrics).toInt()
                drawable?.setBounds(0, 0, value, value)
                holder.mTvSubTitle.setCompoundDrawables(drawable, null, null, null)

                holder.mTvTips.text = "设置"
                holder.mCbBox.isEnabled = true
            } else {
                holder.mTvSubTitle.text = mContext?.getString(R.string.config_bind_fail)
                val drawable = mContext?.resources?.getDrawable(R.drawable.config_command_ic_fail_s, null)
                val value = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18f, mContext?.resources?.displayMetrics).toInt()
                drawable?.setBounds(0, 0, value, value)
                holder.mTvSubTitle.setCompoundDrawables(drawable, null, null, null)

                holder.mTvTips.text = mContext?.getString(R.string.config_check_cause)
                holder.mCbBox.isEnabled = false
            }

            if (position == itemCount - 1) {
                holder.mViewLine.visibility = View.GONE
            } else {
                holder.mViewLine.visibility = View.VISIBLE
            }
            holder.mCbBox.isChecked = mSelectRbList[position]

            holder.mCbBox.setOnClickListener {
                if (selectPosition != -1 && selectPosition != position) {
                    mSelectRbList[selectPosition] = false
                    notifyItemChanged(selectPosition)
                }
                selectPosition = position
                mSelectRbList[selectPosition] = holder.mCbBox.isChecked
                Toast.makeText(mContext, "isCheck = ${holder.mCbBox.isChecked}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addNewData(mutableList: MutableList<DeviceInfo>) {
        mData?.clear()
        mData?.addAll(mutableList)
        for (index in 0 until mutableList.size) {
            mSelectRbList.add(false)
        }
        notifyDataSetChanged()
    }

    fun addData(mutableList: MutableList<DeviceInfo>) {
        mData?.addAll(mutableList)
        for (index in 0 until mutableList.size) {
            mSelectRbList.add(false)
        }
        notifyDataSetChanged()
    }

    fun clear() {
        mData?.clear()
    }

    inner class ViewHolder(parent: View): RecyclerView.ViewHolder(parent) {
        val mIvIcon: ImageView = parent.findViewById(R.id.iv_icon)
        val mTvTitle: TextView = parent.findViewById(R.id.tv_title)
        val mTvSubTitle: TextView = parent.findViewById(R.id.tv_subtitle)
        val mTvTips: TextView = parent.findViewById(R.id.tv_tips)
        val mViewLine: View = parent.findViewById(R.id.view_line)
        val mCbBox: CheckBox = parent.findViewById(R.id.cb_arrow)
    }
}