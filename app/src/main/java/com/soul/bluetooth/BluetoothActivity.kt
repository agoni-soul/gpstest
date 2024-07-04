package com.soul.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.soul.base.BaseMvvmActivity
import com.soul.base.BaseViewModel
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityBluetoothBinding


/**
 *     author : yangzy33
 *     time   : 2024-07-04
 *     desc   :
 *     version: 1.0
 */
class BluetoothActivity: BaseMvvmActivity<ActivityBluetoothBinding, BaseViewModel>() {

    private var mBluetoothReceiver: BluetoothReceiver? = null

    private var mBleAdapter: BleAdapter? = null

    private var mBleDevices = mutableListOf<BluetoothDevice>()

    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_bluetooth

    override fun initView() {
        mBleAdapter = BleAdapter(mBleDevices)
        mViewDataBinding?.rvBluetooth?.let {
            it.adapter = mBleAdapter
            val layoutManager = LinearLayoutManager(mContext).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            it.layoutManager = layoutManager
        }
    }

    override fun initData() {
        mBluetoothReceiver = BluetoothReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mBluetoothReceiver, intentFilter)
    }

    inner class BluetoothReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context ?: return
            intent ?: return
            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!mBleDevices.contains(it)) {
                        mBleDevices.add(it)
                    }
                }
            }
        }
    }
}