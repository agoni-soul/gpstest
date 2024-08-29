package com.soul.bluetooth.adapter

import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.soul.blesdk.bean.BleScanResult
import com.soul.gpstest.R
import com.soul.ui.textView.FoldTextView


/**
 *     author : yangzy33
 *     time   : 2024-08-19
 *     desc   :
 *     version: 1.0
 */
class BleScanAdapterV2(bleDevices: MutableList<BleScanResult>, layoutResId: Int) :
    BaseQuickAdapter<BleScanResult, BaseViewHolder>(layoutResId, bleDevices) {
    private val TAG = javaClass.simpleName

    override fun convert(holder: BaseViewHolder, item: BleScanResult) {
        holder.setText(R.id.tv_ble_name, item.name)
        holder.setText(R.id.tv_ble_mac, item.mac)
        holder.setText(R.id.tv_ble_bond_status, context.getString(R.string.ble_bond_status,item.bondStateStr))
        holder.setText(R.id.tv_ble_connectable,  context.getString(
            R.string.ble_connectable,
            if (item.isConnectable) "true" else "false"
        ))
        val itemTvBleServiceUuids = holder.getView<FoldTextView>(R.id.tv_service_uuids).init()
        if (item.serviceUuids.isEmpty()) {
            itemTvBleServiceUuids.visibility = View.GONE
        } else {
            itemTvBleServiceUuids.visibility = View.VISIBLE
            itemTvBleServiceUuids.setOriginalText(context.getString(R.string.ble_service_uuids, item.serviceUuids.toString()))
        }
        val itemTvBleDeviceUuids = holder.getView<FoldTextView>(R.id.tv_device_uuids).init()
        if (item.deviceUuids.isEmpty()) {
            itemTvBleDeviceUuids.visibility = View.GONE
        } else {
            itemTvBleDeviceUuids.visibility = View.VISIBLE
            itemTvBleDeviceUuids.setOriginalText(context.getString(R.string.ble_device_uuids, item.deviceUuids.toString()))
        }
        val itemTvBleDataByte = holder.getView<FoldTextView>(R.id.tv_ble_data).init()
        if (item.dataHexDetail.isNullOrEmpty()) {
            itemTvBleDataByte.visibility = View.GONE
        } else {
            itemTvBleDataByte.visibility = View.VISIBLE
            itemTvBleDataByte.setOriginalText(item.dataHexDetail)
        }
        addChildClickViewIds(R.id.tv_service_uuids, R.id.tv_device_uuids, R.id.tv_ble_data)
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