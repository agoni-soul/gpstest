package com.soul.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-07-04
 *     desc   :
 *     version: 1.0
 */
class BleAdapter(bleDevices: MutableList<BluetoothDevice>) :
    RecyclerView.Adapter<BleAdapter.BleViewHolder>() {
    private val TAG = javaClass.simpleName

    private lateinit var mContext: Context

    private val mBleDevices = bleDevices

    private val mBluetoothAdapter: BluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleAdapter.BleViewHolder {
        mContext = parent.context
        val view = LayoutInflater.from(mContext).inflate(R.layout.adapter_item_ble, null, false)
        return BleViewHolder(view)
    }

    override fun getItemCount(): Int = mBleDevices.size

    override fun onBindViewHolder(holder: BleAdapter.BleViewHolder, position: Int) {
        holder.itemTvBleName.text = mBleDevices[position].name
        holder.itemTvBleMac.text = mBleDevices[position].address
        holder.itemView.setOnClickListener {
            val ble = mBleDevices[position]
            Log.d(
                TAG,
                "onBindViewHolder: BluetoothDevice: name = ${ble.name}, address = ${ble.address}, type = ${ble.type}, alias = ${ble.alias}, bondState = ${ble.bondState}, bluetoothClass = ${ble.bluetoothClass}"
            )
            if (BluetoothAdapter.checkBluetoothAddress(ble.address)) {
//                ble.connectGatt()
            }
        }
    }

    inner class BleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemTvBleName: TextView
        var itemTvBleMac: TextView

        init {
            itemTvBleName = itemView.findViewById(R.id.tv_ble_name)
            itemTvBleMac = itemView.findViewById(R.id.tv_ble_mac)
        }
    }
}