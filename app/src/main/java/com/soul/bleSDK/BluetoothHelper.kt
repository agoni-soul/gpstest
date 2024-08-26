package com.soul.bleSDK

import android.app.Activity
import android.bluetooth.*
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.ContactsContract
import android.util.Log
import com.soul.bleSDK.manager.BleScanManager

/**
 *     author : yangzy33
 *     time   : 2024-08-02
 *     desc   :
 *     version: 1.0
 */

public class BluetoothHelper(context: Context) {
    private val TAG = this.javaClass::class.java.simpleName

    private var bluetoothHeadset: BluetoothHeadset? = null

    private val mReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            context ?: return
            intent ?: return
            val action = intent.action ?: return
            val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?
            if (BluetoothDevice.ACTION_ACL_CONNECTED == action) {
                // 获取设备音量
//                getDeviceVolume(device)
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
                // 处理设备断开连接的情况
            }
        }
    }

    private val bluetoothServiceHandler: Handler by lazy {
        object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                Log.d(TAG, "handleMessage: msg = $msg")
                when (msg.what) {
                }
            }
        }
    }

    init {
        BleScanManager.getBluetoothAdapter()
            ?.getProfileProxy(context, object : BluetoothProfile.ServiceListener {

                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                    bluetoothHeadset = proxy as? BluetoothHeadset
                }

                override fun onServiceDisconnected(profile: Int) {
                    bluetoothHeadset = null
                }
            }, BluetoothProfile.HEADSET)
        // 注册蓝牙设备状态变化的广播接收器
        val filter = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        context.registerReceiver(mReceiver, filter);
    }

    fun getContact(activity: Activity?) {
        val contentResolver = activity?.contentResolver
        val cursor = contentResolver?.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val index = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                if (index >= 0) {
                    val contactId = cursor.getString(index)
                    Log.d(TAG, "getContact: contactId = $contactId")
                }
            }
            cursor.close()
        }
    }
}