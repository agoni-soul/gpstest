package com.soul.blesdk.manager

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import com.blankj.utilcode.util.Utils
import com.soul.BleSDKApplication

/**
 *
 * @author haha
 * @date 2024-08-26
 * @version 1.0
 *
 */
open class BaseBleManager {
    protected val TAG: String = javaClass.simpleName
    protected var mBleManager: BluetoothManager? = null
    protected var mBleAdapter: BluetoothAdapter? = null
    protected var mApplication: Application? = null

    init {
        mApplication = BleSDKApplication.application ?: Utils.getApp()
        Log.d(TAG, "application = $mApplication")
        mBleManager = mApplication?.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager?
        mBleAdapter =  mBleManager?.adapter
    }

    fun getBluetoothManager(): BluetoothManager? = mBleManager

    fun getBluetoothAdapter(): BluetoothAdapter? = mBleAdapter
}