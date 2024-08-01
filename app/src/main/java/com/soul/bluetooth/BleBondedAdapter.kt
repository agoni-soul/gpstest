package com.soul.bluetooth

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.soul.bean.BleScanResult
import com.soul.gpstest.R


/**
 *     author : yangzy33
 *     time   : 2024-08-01
 *     desc   :
 *     version: 1.0
 */
class BleBondedAdapter(bleBondedDevices: MutableList<BleScanResult>, layoutResId: Int) :
    BaseQuickAdapter<BleScanResult, BaseViewHolder>(layoutResId, bleBondedDevices) {

    private var mItemClickCallback:ItemClickCallback? = null

    fun setCallback(itemClickCallback: ItemClickCallback?) {
        mItemClickCallback = itemClickCallback
    }

    override fun convert(holder: BaseViewHolder, item: BleScanResult) {
        holder.setText(R.id.tv_ble_name, item.name)
        holder.setText(R.id.tv_ble_mac, item.mac)
        holder.setText(R.id.tv_ble_bond_status, context.getString(R.string.ble_bond_status,item.bondStateStr))
        holder.setText(R.id.tv_ble_connectable,  context.getString(
            R.string.ble_connectable,
            if (item.isConnectable) "true" else "false"
        ))
        val itemTvBleServiceUuids = holder.getView<TextView>(R.id.tv_service_uuids)
        if (item.serviceUuids.isEmpty()) {
            itemTvBleServiceUuids.visibility = View.GONE
        } else {
            itemTvBleServiceUuids.visibility = View.VISIBLE
            itemTvBleServiceUuids.text = context.getString(R.string.ble_service_uuids, item.serviceUuids.toString())
        }
        val itemTvBleDeviceUuids = holder.getView<TextView>(R.id.tv_device_uuids)
        if (item.deviceUuids.isEmpty()) {
            itemTvBleDeviceUuids.visibility = View.GONE
        } else {
            itemTvBleDeviceUuids.visibility = View.VISIBLE
            itemTvBleDeviceUuids.text = context.getString(R.string.ble_device_uuids, item.deviceUuids.toString())
        }
        val itemTvBleDataByte = holder.getView<TextView>(R.id.tv_ble_data)
        if (item.dataHexDetail.isNullOrEmpty()) {
            itemTvBleDataByte.visibility = View.GONE
        } else {
            itemTvBleDataByte.visibility = View.VISIBLE
            itemTvBleDataByte.text = item.dataHexDetail
        }
        holder.getView<Button>(R.id.btn_ble_unbind).setOnClickListener {
            mItemClickCallback?.onClickUnbind(item)
        }
        holder.getView<Button>(R.id.btn_ble_communicate).setOnClickListener {
            mItemClickCallback?.onClickCommunicate(item)
        }
    }

    interface ItemClickCallback {
        fun onClickUnbind(result: BleScanResult)
        fun onClickCommunicate(result: BleScanResult)
    }
}