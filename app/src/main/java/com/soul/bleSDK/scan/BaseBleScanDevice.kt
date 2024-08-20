package com.soul.bleSDK.scan

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import com.soul.SoulApplication
import com.soul.base.BaseActivity
import com.soul.bleSDK.interfaces.IBleScanCallback
import com.soul.log.DOFLogUtil
import com.soul.util.PermissionUtils


/**
 *     author : yangzy33
 *     time   : 2024-08-20
 *     desc   :
 *     version: 1.0
 */
open class BaseBleScanDevice: IBleScanDevice {
    protected val TAG = javaClass.simpleName
    private var mBleManager: BluetoothManager? = null
    protected var mBleAdapter: BluetoothAdapter? = null
    protected var mIsScanning = false
    protected var mBleScanCallbacks = mutableListOf<IBleScanCallback>()
    protected var mBluetoothReceiver: BluetoothReceiver? = null
    private val mIntentFilterList = mutableListOf<String>()

    init {
        Log.d(TAG, "application = ${SoulApplication.application}")
        mBleManager = SoulApplication.application?.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager?
        mBleAdapter =  mBleManager?.adapter
    }

    override fun getBluetoothManager(): BluetoothManager? = mBleManager

    override fun getBluetoothAdapter(): BluetoothAdapter? = mBleAdapter

    override fun startScan() {
    }

    override fun stopScan() {
    }

    override fun isScanning(): Boolean = mIsScanning

    override fun setCallback(callback: IBleScanCallback) {
        mBleScanCallbacks.add(callback)
    }

    /**
     * 打开手机蓝牙
     *
     * @return true 表示打开成功
     */
    fun isEnableBle(): Boolean {
        if (mBleAdapter?.isEnabled == false) {
            if (!PermissionUtils.checkSinglePermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                DOFLogUtil.d(TAG, "Manifest.permission.BLUETOOTH_CONNECT: PERMISSION_DENIED")
                return false
            }
            //若未打开手机蓝牙，则会弹出一个系统的是否打开/关闭蓝牙的对话框，禁止或者未处理返回false，允许返回true
            //若已打开手机蓝牙，直接返回true
            val enableState: Boolean = mBleAdapter!!.enable()
            Log.d(TAG, "（用户操作）手机蓝牙是否打开成功：$enableState")
            return enableState
        } else {
            return true
        }
    }

    fun getBluetoothReceiver(): BluetoothReceiver? = mBluetoothReceiver

    fun addFilter(action: String) {
        mIntentFilterList.add(action)
    }

    fun registerBleReceiver(activity: BaseActivity?) {
        activity ?: return
        if (activity.isFinishing || activity.isDestroyed) return
        mBluetoothReceiver = BluetoothReceiver()
        val intentFilter = IntentFilter()
        mIntentFilterList.forEach {
            intentFilter.addAction(it)
        }
        activity.registerReceiver(mBluetoothReceiver!!, intentFilter)
    }

    fun unregisterBleReceiver(activity: BaseActivity?) {
        activity ?: return
        if (activity.isFinishing || activity.isDestroyed) return
        activity.unregisterReceiver(mBluetoothReceiver)
    }
}