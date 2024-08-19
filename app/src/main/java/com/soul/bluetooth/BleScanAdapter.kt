package com.soul.bluetooth

import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soul.bean.BleScanResult
import com.soul.gpstest.R
import com.soul.ui.textView.FoldTextView


/**
 *     author : yangzy33
 *     time   : 2024-07-04
 *     desc   :
 *     version: 1.0
 */
class BleScanAdapter(bleDevices: MutableList<BleScanResult>) :
    RecyclerView.Adapter<BleScanAdapter.BleViewHolder>() {
    private val TAG = this.javaClass::class.simpleName

    private lateinit var mContext: Context

    private val mBleDevices = bleDevices

    private lateinit var mBleManager: BluetoothManager

    private var mItemClickCallback: ItemClickCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleScanAdapter.BleViewHolder {
        mContext = parent.context
        mBleManager = mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val view = LayoutInflater.from(mContext).inflate(R.layout.adapter_item_ble_scan, parent, false)
        return BleViewHolder(view)
    }

    override fun getItemCount(): Int = mBleDevices.size

    override fun onBindViewHolder(holder: BleScanAdapter.BleViewHolder, position: Int) {
        val bleScanResult = mBleDevices[position]
        holder.itemTvBleName.text = bleScanResult.name
        holder.itemTvBleMac.text = bleScanResult.mac
        holder.itemTvBleBondStatus.text =
            mContext.getString(R.string.ble_bond_status, mBleDevices[position].bondStateStr)
        holder.itemTvBleConnectable.text = mContext.getString(
            R.string.ble_connectable,
            if (mBleDevices[position].isConnectable) "true" else "false"
        )
        if (bleScanResult.serviceUuids.isEmpty()) {
            holder.itemTvBleServiceUuids.visibility = View.GONE
        } else {
            holder.itemTvBleServiceUuids.visibility = View.VISIBLE
            holder.itemTvBleServiceUuids.setOriginalText(mContext.getString(R.string.ble_service_uuids, bleScanResult.serviceUuids.toString()))
        }
        if (bleScanResult.deviceUuids.isEmpty()) {
            holder.itemTvBleDeviceUuids.visibility = View.GONE
        } else {
            holder.itemTvBleDeviceUuids.visibility = View.VISIBLE
            holder.itemTvBleDeviceUuids.setOriginalText(mContext.getString(R.string.ble_device_uuids, bleScanResult.deviceUuids.toString()))
        }
        if (bleScanResult.dataHexDetail.isNullOrEmpty()) {
            holder.itemTvBleDataByte.visibility = View.GONE
        } else {
            holder.itemTvBleDataByte.visibility = View.VISIBLE
            holder.itemTvBleDataByte.setOriginalText(bleScanResult.dataHexDetail)
        }
        holder.itemView.setOnClickListener {
            val result = mBleDevices[position]
            Log.d(TAG, "onBindViewHolder: result =\n$result")
            mItemClickCallback?.onClick(result)
        }
        holder.itemTvBleDeviceUuids.setOnClickListener {
            this@BleScanAdapter.notifyItemChanged(position)
        }
        holder.itemTvBleServiceUuids.setOnClickListener {
            this@BleScanAdapter.notifyItemChanged(position)
        }
        holder.itemTvBleDataByte.setOnClickListener {
            this@BleScanAdapter.notifyItemChanged(position)
        }
    }

    fun setCallback(itemClickCallback: ItemClickCallback?) {
        mItemClickCallback = itemClickCallback
    }

    inner class BleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemTvBleName: TextView
        var itemTvBleMac: TextView
        var itemTvBleDataByte: FoldTextView
        var itemTvBleBondStatus: TextView
        var itemTvBleConnectable: TextView
        var itemTvBleServiceUuids: FoldTextView
        var itemTvBleDeviceUuids: FoldTextView

        init {
            itemTvBleName = itemView.findViewById(R.id.tv_ble_name)
            itemTvBleMac = itemView.findViewById(R.id.tv_ble_mac)
            itemTvBleDataByte = itemView.findViewById<FoldTextView>(R.id.tv_ble_data).init()
            itemTvBleBondStatus = itemView.findViewById(R.id.tv_ble_bond_status)
            itemTvBleConnectable = itemView.findViewById(R.id.tv_ble_connectable)
            itemTvBleServiceUuids = itemView.findViewById<FoldTextView>(R.id.tv_service_uuids).init()
            itemTvBleDeviceUuids = itemView.findViewById<FoldTextView>(R.id.tv_device_uuids).init()
        }
    }

    interface ItemClickCallback {
        fun onClick(result: BleScanResult)
    }

    private fun FoldTextView.init(): FoldTextView {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val width: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.width ?: 0
        } else {
            windowManager.defaultDisplay.width
        }
        initWidth(width)
        maxLines = 3
        setHasAnimation(true)
        setCloseInNewLine(true)
        setOpenSuffixColor(context.resources.getColor(R.color.cyan))
        setCloseSuffixColor(context.resources.getColor(R.color.yellow))
        return this
    }
}