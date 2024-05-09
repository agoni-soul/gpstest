package com.soul.ui.dialog

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.soul.gpstest.R

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/08/19
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class CustomDialogFragment(): DialogFragment() {
    private lateinit var mRvPermissions: RecyclerView

    private var mDismissListener: OnDismissListener? = null

    private val mHaveOpenedPermissionSet: MutableSet<Int> = HashSet()

    private var mContext: Context? = null

    constructor(context: Context): this() {
        mContext = context
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("haha", "onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("haha", "onResume")
    }

    override fun onResume() {
        super.onResume()
        Log.d("haha", "onResume")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("haha", "onCreateView")
        dialog?.window?.decorView?.background = ColorDrawable(Color.TRANSPARENT)
        val holder = inflater.inflate(R.layout.dialog_fragment_custom, container, false)
        holder.findViewById<ImageView>(R.id.imgCancel).setOnClickListener {
            dismiss()
        }
        mRvPermissions = holder.findViewById(R.id.rv_permissions)
        mRvPermissions.layoutManager = LinearLayoutManager(mContext)
//        mPermissionAdapter = PermissionSwitchDialogAdapter(mContext, mNoOpenPermissions)
//        mPermissionAdapter.setClickListener(mPermissionListenerMap)
//        mRvPermissions.adapter = mPermissionAdapter
        return holder
    }

    override fun onStart() {
        super.onStart()
        Log.d("haha", "onStart")
        dialog?.window?.apply {
            decorView.setPadding(0, 0, 0, 0)
            val layoutParams = attributes
            layoutParams.gravity = Gravity.BOTTOM
            layoutParams.height = 150
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            attributes = layoutParams
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            if (!manager.isStateSaved) {
                super.show(manager, tag)
                Log.d("haha", "show")
            }
        } catch (ignore: IllegalStateException) {
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("haha", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("haha", "onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("haha", "onDestroyView")
    }

    override fun dismiss() {
        try {
            super.dismiss()
        } catch (ignore: IllegalStateException) {
        }
    }
}

//class PermissionSwitchDialogAdapter() :
//    RecyclerView.Adapter<PermissionSwitchDialogAdapter.PermissionDialogViewHolder>() {
//    private var mContext: Context? = null
//
//    private val mPermissionBeanList: MutableList<PermissionBean> = ArrayList()
//
//    private var mPermissionListenerMap: MutableMap<Int, View.OnClickListener> = HashMap()
//
//    constructor(context: Context?, permissions: MutableList<Int>) : this() {
//        mContext = context
//        for (permission in permissions) {
//            mPermissionBeanList.add(getPermissionBean(permission))
//        }
//    }
//
//    fun getPermissionBean(permission: Int): PermissionBean {
//        when (permission) {
//            CheckPermissionConstant.CHECK_AUTHORIZE_CAMERA -> {
//                return mContext?.let {
//                    PermissionBean(permission, R.drawable.config_net_ic_camera,
//                        it.getString(R.string.config_permission_camera_title),
//                        isOpen = false,
//                        noOpenTip = it.getString(R.string.config_goto_open),
//                        openTip = it.getString(R.string.config_authorized))
//                } ?: PermissionBean(permission)
//            }
//            CheckPermissionConstant.CHECK_AUTHORIZE_GPS -> {
//                return mContext?.let {
//                    PermissionBean(permission, R.drawable.config_net_ic_location,
//                        it.getString(R.string.config_permission_authentic_gps_title),
//                        isOpen = false,
//                        noOpenTip = it.getString(R.string.config_goto_open),
//                        openTip = it.getString(R.string.config_authorized))
//                } ?: PermissionBean(permission)
//            }
//            CheckPermissionConstant.CHECK_OPEN_GPS -> {
//                return mContext?.let {
//                    PermissionBean(permission, R.drawable.config_net_ic_place,
//                        it.getString(R.string.config_permission_open_gps_title),
//                        isOpen = false,
//                        noOpenTip = it.getString(R.string.config_goto_open),
//                        openTip = it.getString(R.string.config_opened))
//                } ?: PermissionBean(permission)
//            }
//            CheckPermissionConstant.CHECK_OPEN_BLE -> {
//                return mContext?.let {
//                    PermissionBean(permission, R.drawable.config_net_ic_blueteeth,
//                        it.getString(R.string.config_permission_ble_title),
//                        isOpen = false,
//                        noOpenTip = it.getString(R.string.config_goto_open),
//                        openTip = it.getString(R.string.config_opened))
//                } ?: PermissionBean(permission)
//            }
//            CheckPermissionConstant.CHECK_OPEN_WIFI -> {
//                return mContext?.let {
//                    PermissionBean(permission, R.drawable.config_net_ic_wlan,
//                        it.getString(R.string.config_permission_wlan_title),
//                        isOpen = false,
//                        noOpenTip = it.getString(R.string.config_goto_open),
//                        openTip = it.getString(R.string.config_opened))
//                } ?: PermissionBean(permission)
//            }
//            else -> {
//                return PermissionBean(permission)
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionDialogViewHolder {
//        val holder = LayoutInflater.from(parent.context).inflate(R.layout.config_item_permission_switch_dialog, parent, false)
//        return PermissionDialogViewHolder(holder)
//    }
//
//    override fun onBindViewHolder(holder: PermissionDialogViewHolder, position: Int) {
//        val permissionBean = mPermissionBeanList[position]
//
//        permissionBean.permissionDrawableId?.let {
//            holder.mPermissionIcon.setImageResource(it)
//        }
//        holder.mPermissionTitle.text = permissionBean.title
//        holder.mPermissionOpen.let {
//
//            if (permissionBean.isOpen) {
//                it.text = permissionBean.openTip
//                it.isClickable = false
//                mContext?.resources?.let { resources ->
//                    it.setTextColor(resources.getColor(R.color.config_text_tip_color))
//                    it.background = resources.getDrawable(R.drawable.config_background_shape_no_shadow_grey_15, null)
//                    val drawable = resources.getDrawable(R.drawable.config_net_ic_checked, null)
//                    drawable.setBounds(ScreenUtil.dip2px(mContext, 6F), 0, drawable.minimumWidth, drawable.minimumHeight)
//                    it.setCompoundDrawables(drawable, null, null, null)
//                }
//            } else {
//                it.text = permissionBean.noOpenTip
//                it.isClickable = true
//                mContext?.resources?.let { resources ->
//                    it.setTextColor(resources.getColor(R.color.config_background_button_select_color))
//                    it.background = resources.getDrawable(R.drawable.config_background_shape_no_shadow_blue_15, null)
//                }
//                it.setCompoundDrawables(null, null, null, null)
//            }
//            it.setOnClickListener(mPermissionListenerMap[permissionBean.permission])
//        }
//    }
//
//    fun isEnabled(position: Int): Boolean {
//        return false
//    }
//
//    open fun setClickListener(permissionListenerMap: MutableMap<Int, View.OnClickListener>) {
//        mPermissionListenerMap = permissionListenerMap
//    }
//
//    fun updatePermissionStatus(position: Int, isOpen: Boolean) {
//        mPermissionBeanList[position].isOpen = isOpen
//    }
//
//    override fun getItemCount(): Int = mPermissionBeanList.size
//
//    override fun getItemId(position: Int): Long = position.toLong()
//
//    class PermissionDialogViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {
//        var mPermissionIcon: ImageView = parent.findViewById(R.id.iv_permission_icon)
//        var mPermissionTitle: TextView = parent.findViewById(R.id.tv_permission_title)
//        var mPermissionOpen: Button = parent.findViewById(R.id.bt_permission_open)
//    }
//}

interface OnDismissListener {
    fun onDialogDismiss()
}