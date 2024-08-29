package com.soul.blesdk.scan

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.IntentFilter
import com.soul.blesdk.interfaces.IBleScanCallback
import com.soul.blesdk.manager.BleScanManager
import java.util.Collections.synchronizedMap


/**
 *     author : haha
 *     time   : 2024-08-20
 *     desc   :
 *     version: 1.0
 */
open class BaseBleScanDevice: IBleScanDevice {
    protected val TAG = javaClass.simpleName
    protected var mIsScanning = false
    protected var mBleScanCallbackMap = synchronizedMap(HashMap<String, IBleScanCallback?>())
    private var mBluetoothReceiver: BluetoothReceiver? = null
    private val mIntentFilterList = mutableListOf<String>()

    override fun getBluetoothManager(): BluetoothManager? =
        BleScanManager.getInstance()?.getBluetoothManager()

    override fun getBluetoothAdapter(): BluetoothAdapter? =
        BleScanManager.getInstance()?.getBluetoothAdapter()

    override fun getBleBondedDevices(): Set<BluetoothDevice>? =
        BleScanManager.getInstance()?.getBondedDevices()

    override fun startScan(tag: String?) {
    }

    override fun stopScan(tag: String?) {
        mBleScanCallbackMap.remove(tag)
    }

    override fun isScanning(tag: String?): Boolean = mIsScanning

    override fun setCallback(tag: String, callback: IBleScanCallback?) {
        mBleScanCallbackMap[tag] = callback
    }

    fun getBluetoothReceiver(): BluetoothReceiver? = mBluetoothReceiver

    fun addFilter(action: String) {
        mIntentFilterList.add(action)
    }

    fun registerBleReceiver(activity: Activity?) {
        activity ?: return
        if (activity.isFinishing || activity.isDestroyed) return
        mBluetoothReceiver = BluetoothReceiver()
        val intentFilter = IntentFilter()
        mIntentFilterList.forEach {
            intentFilter.addAction(it)
        }
        activity.registerReceiver(mBluetoothReceiver!!, intentFilter)
    }

    fun unregisterBleReceiver(activity: Activity?) {
        activity ?: return
        if (activity.isFinishing || activity.isDestroyed) return
        activity.unregisterReceiver(mBluetoothReceiver)
    }
}