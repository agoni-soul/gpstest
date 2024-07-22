package com.soul.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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
                "onBindViewHolder: BluetoothDevice: name = ${ble.name}, address = ${ble.address}, type = ${bleTypeStr(ble.type)}, typeDevice = ${bleTypeDeviceStr(ble.type)}, alias = ${ble.alias}, bondState = ${bleBoundState(ble.bondState)}, bluetoothClass = ${ble.bluetoothClass}"
            )
            if (BluetoothAdapter.checkBluetoothAddress(ble.address)) {
//                ble.connectGatt()
            }
        }
    }

    private fun bleTypeDeviceStr(type: Int): String {
        return when (type) {
            0 -> {
                "Unknown"
            }
            1 -> {
                "Classic - BR/EDR devices"
            }
            2 -> {
                "Low Energy - LE-only"
            }
            3 -> {
                "Dual Mode - BR/EDR/LE"
            }
            else -> {
                "Unknown"
            }
        }
    }

    private fun bleTypeStr(type: Int): String {
        return when (type) {
            0 -> {
                "DEVICE_TYPE_UNKNOWN"
            }
            1 -> {
                "DEVICE_TYPE_CLASSIC"
            }
            2 -> {
                "DEVICE_TYPE_LE"
            }
            3 -> {
                "DEVICE_TYPE_DUAL"
            }
            else -> {
                "DEVICE_TYPE_UNKNOWN"
            }
        }
    }

    private fun bleBoundState(state: Int): String {
        return when (state) {
            10 -> {
                "BOND_NONE"
            }
            11 -> {
                "BOND_BONDING"
            }
            12 -> {
                "BOND_BONDED"
            }
            else -> {
                "BOND_NONE"
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