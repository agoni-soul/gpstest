package com.soul.bluetooth

import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soul.bean.BleScanResult
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-07-04
 *     desc   :
 *     version: 1.0
 */
class BleAdapter(bleDevices: MutableList<BleScanResult>) :
    RecyclerView.Adapter<BleAdapter.BleViewHolder>() {
    private val TAG = javaClass.simpleName

    private lateinit var mContext: Context

    private val mBleDevices = bleDevices

    private lateinit var mBleManager: BluetoothManager

    private var mItemClickCallback: ItemClickCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleAdapter.BleViewHolder {
        mContext = parent.context
        mBleManager = mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val view = LayoutInflater.from(mContext).inflate(R.layout.adapter_item_ble, parent, false)
        return BleViewHolder(view)
    }

    override fun getItemCount(): Int = mBleDevices.size

    override fun onBindViewHolder(holder: BleAdapter.BleViewHolder, position: Int) {
        holder.itemTvBleName.text = mBleDevices[position].name
        holder.itemTvBleMac.text = mBleDevices[position].mac
        holder.itemTvBleDataByte.text = mBleDevices[position].dataHexDetail
        holder.itemTvBleBondStatus.text =
            mContext.getString(R.string.ble_bond_status, mBleDevices[position].bondStateStr)
        holder.itemTvBleConnectable.text = mContext.getString(
            R.string.ble_connectable,
            if (mBleDevices[position].isConnectable) "true" else "false"
        )
        holder.itemView.setOnClickListener {
            val result = mBleDevices[position]
            Log.d(TAG, "onBindViewHolder: result =\n$result")
            mItemClickCallback?.onClick(result)
        }
    }

    fun setCallback(itemClickCallback: ItemClickCallback?) {
        mItemClickCallback = itemClickCallback
    }

    inner class BleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemTvBleName: TextView
        var itemTvBleMac: TextView
        var itemTvBleDataByte: TextView
        var itemTvBleBondStatus: TextView
        var itemTvBleConnectable: TextView

        init {
            itemTvBleName = itemView.findViewById(R.id.tv_ble_name)
            itemTvBleMac = itemView.findViewById(R.id.tv_ble_mac)
            itemTvBleDataByte = itemView.findViewById(R.id.tv_ble_data)
            itemTvBleBondStatus = itemView.findViewById(R.id.tv_ble_bond_status)
            itemTvBleConnectable = itemView.findViewById(R.id.tv_ble_connectable)
        }
    }

    interface ItemClickCallback {
        fun onClick(result: BleScanResult)
    }
}