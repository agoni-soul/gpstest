package com.soul.bleSDK.scan

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.soul.bean.toBleScanResult
import com.soul.bleSDK.constants.ScanSettings
import com.soul.bleSDK.interfaces.IBleScanCallback
import com.soul.bleSDK.manager.BleScanManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/**
 *     author : yangzy33
 *     time   : 2024-08-20
 *     desc   :
 *     version: 1.0
 */
class BluetoothReceiver: BroadcastReceiver() {
    private val TAG = javaClass.simpleName
    private var mBleScanCallback: IBleScanCallback? = null
    private var mBleBoundCallback: IBleScanCallback? = null

    fun setBleScanCallback(bleScanCallback: IBleScanCallback?) {
        mBleScanCallback = bleScanCallback
    }

    fun setBleBoundCallback(bleBoundCallback: IBleScanCallback?) {
        mBleBoundCallback = bleBoundCallback
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return
        intent.action ?: return
        when (intent.action) {
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                MainScope().launch(Dispatchers.IO) {
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                    val preState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)
                    val bleDevice: BluetoothDevice =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        } ?: return@launch
                    Log.d(TAG, "BluetoothReceiver: state = $state, preState = $preState, device.name = ${bleDevice.name}, device.mac = ${bleDevice.address}")
                    val bondedDevices = BleScanManager.getBondedDevices() ?: return@launch
                    if (state == BluetoothDevice.BOND_BONDED &&
                        (preState == BluetoothDevice.BOND_NONE || preState == BluetoothDevice.BOND_BONDING)) {
                        if (bondedDevices.find { it.address == bleDevice.address } != null) {
                            mBleBoundCallback?.onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES.callbackType, bleDevice.toBleScanResult())
                        }
                    } else if (state == BluetoothDevice.BOND_NONE &&
                        (preState == BluetoothDevice.BOND_BONDED || preState == BluetoothDevice.BOND_BONDING)) {
                        mBleBoundCallback?.onScanResult(ScanSettings.CALLBACK_TYPE_REMOVE_BOUND_DEVICE.callbackType, bleDevice.toBleScanResult())
                    }
                }
            }
            BluetoothDevice.ACTION_FOUND -> {
                MainScope().launch(Dispatchers.IO) {
                    val bleDevice: BluetoothDevice =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                BluetoothDevice.EXTRA_DEVICE,
                                BluetoothDevice::class.java
                            )
                        } else {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        } ?: return@launch
                    Log.d(TAG, "BluetoothReceiver: device.name = ${bleDevice.name}, device.mac = ${bleDevice.address}")
                    mBleScanCallback?.onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES.callbackType, bleDevice.toBleScanResult())
                }
            }
        }
    }
}