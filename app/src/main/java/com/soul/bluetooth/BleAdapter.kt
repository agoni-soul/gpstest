package com.soul.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.soul.gpstest.R
import com.soul.util.PermissionUtils


/**
 *     author : yangzy33
 *     time   : 2024-07-04
 *     desc   :
 *     version: 1.0
 */
class BleAdapter(bleDevices: MutableList<BluetoothDevice>): RecyclerView.Adapter<BleAdapter.BleViewHolder>() {
    private lateinit var mContext: Context

    private val mBleDevices = bleDevices

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleAdapter.BleViewHolder {
        mContext = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_item_ble, parent, false)
        return BleViewHolder(view)
    }

    override fun getItemCount(): Int = mBleDevices.size

    override fun onBindViewHolder(holder: BleAdapter.BleViewHolder, position: Int) {
        if (PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            holder.itemTvBleName.text = mBleDevices[position].name
            holder.itemTvBleMac.text = mBleDevices[position].address
        } else {
            Toast.makeText(mContext, "BLUETOOTH_CONNECT 没有授权", Toast.LENGTH_SHORT).show()
        }
    }

    inner class BleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var itemTvBleName: TextView
        var itemTvBleMac: TextView

        init {
            itemTvBleName = itemView.findViewById(R.id.tv_ble_name)
            itemTvBleMac = itemView.findViewById(R.id.tv_ble_mac)
        }
    }
}