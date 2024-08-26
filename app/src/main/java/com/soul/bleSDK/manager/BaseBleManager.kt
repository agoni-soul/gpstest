package com.soul.bleSDK.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import com.soul.SoulApplication

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

    init {
        Log.d(TAG, "application = ${SoulApplication.application}")
        mBleManager = SoulApplication.application?.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager?
        mBleAdapter =  mBleManager?.adapter
    }

    fun getBluetoothManager(): BluetoothManager? = mBleManager

    fun getBluetoothAdapter(): BluetoothAdapter? = mBleAdapter
}