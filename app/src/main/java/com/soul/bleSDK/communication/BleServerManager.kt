package com.soul.bleSDK.communication

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.soul.bleSDK.constants.BleConstants
import java.util.UUID


/**
 *     author : haha
 *     time   : 2024-08-22
 *     desc   :
 *     version: 1.0
 */
object BleServerManager {
    private val TAG = javaClass.simpleName

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "服务准备就绪，请搜索广播")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
                Log.d(TAG, "广播数据超过31个字节了 !")
            } else {
                Log.d(TAG, "服务启动失败: $errorCode")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_ADVERTISE)
    fun launchAdvertising(bluetoothAdapter: BluetoothAdapter?) {
        /**
         * GAP广播数据最长只能31个字节，包含两中： 广播数据和扫描回复
         * - 广播数据是必须的，外设需要不断发送广播，让中心设备知道
         * - 扫描回复是可选的，当中心设备扫描到才会扫描回复
         * 广播间隔越长，越省电
         */

        //广播设置
        val advSetting = AdvertiseSettings.Builder()
            //低延时，高功率，不使用后台
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            // 高的发送功率
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            // 可连接
            .setConnectable(true)
            //广播时限。最多180000毫秒。值为0将禁用时间限制。（不设置则为无限广播时长）
            .setTimeout(0)
            .build()
        //设置广播包，这个是必须要设置的
        val advData = AdvertiseData.Builder()
            .setIncludeDeviceName(true) //显示名字
            .setIncludeTxPowerLevel(true)//设置功率
            .addServiceUuid(ParcelUuid(BleConstants.UUID_SERVICE)) //设置 UUID 服务的 uuid
            .build()


        //测试 31bit
        val byteData = byteArrayOf(
            -65, 2, 3, 6, 4, 23, 23, 9, 9,
            9, 1, 2, 3, 6, 4, 23, 23, 9, 9, 8, 23, 23, 23
        )
        //扫描广播数据（可不写，客户端扫描才发送）
        val scanResponse = AdvertiseData.Builder()
            //设置厂商数据
            .addManufacturerData(0x19, byteData)
            .build()

        /**
         * GATT 使用了 ATT 协议，ATT 把 service 和 characteristic 对应的数据保存在一个查询表中，
         * 依次查找每一项的索引
         * BLE 设备通过 Service 和 Characteristic 进行通信
         * 外设只能被一个中心设备连接，一旦连接，就会停止广播，断开又会重新发送
         * 但中心设备同时可以和多个外设连接
         * 他们之间需要双向通信的话，唯一的方式就是建立 GATT 连接
         * 外设作为 GATT(server)，它维持了 ATT 的查找表以及service 和 charateristic 的定义
         */
        val bluetoothLeAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        //开启广播,这个外设就开始发送广播了
        bluetoothLeAdvertiser?.startAdvertising(
            advSetting,
            advData,
            scanResponse,
            advertiseCallback
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_ADVERTISE)
    fun stopAdvertising(bluetoothAdapter: BluetoothAdapter?) {
        bluetoothAdapter?.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
    }

    /**
     * 添加 Gatt service 用来通信
     */
    //开启广播service，这样才能通信，包含一个或多个 characteristic ，每个service 都有一个 uuid
    fun getBleGattService(uuid: UUID, serviceType: Int): BluetoothGattService {
        return BluetoothGattService(uuid, serviceType)
    }

    /**
     * characteristic 是最小的逻辑单元
     * 一个 characteristic 包含一个单一 value 变量 和 0-n个用来描述 characteristic 变量的
     * Descriptor。与 service 相似，每个 characteristic 用 16bit或者32bit的uuid作为标识
     * 实际的通信中，也是通过 Characteristic 进行读写通信的
     */
    //添加读+通知的 GattCharacteristic
    fun createReadGattCharacteristic(gattService: BluetoothGattService? = null, uuid: UUID): BluetoothGattCharacteristic {
        val readGattCharacteristic = createGattCharacteristic(
            uuid,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        addCharacteristic(gattService, readGattCharacteristic)
        return readGattCharacteristic
    }

    //添加写的 GattCharacteristic
    fun createWriteGattCharacteristic(gattService: BluetoothGattService? = null, writeUUID: UUID, describeUUID: UUID? = null): BluetoothGattCharacteristic {
        val writeGattCharacteristic = createGattCharacteristic(
            writeUUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        describeUUID?.let {
            //添加 Descriptor 描述符
            val descriptor =
                BluetoothGattDescriptor(
                    it,
                    BluetoothGattDescriptor.PERMISSION_WRITE
                )
            writeGattCharacteristic.addDescriptor(descriptor)
        }
        addCharacteristic(gattService, writeGattCharacteristic)
        return writeGattCharacteristic
    }

    fun createGattCharacteristic(uuid: UUID, properties: Int, permissions: Int): BluetoothGattCharacteristic {
        return BluetoothGattCharacteristic(uuid, properties, permissions)
    }

    fun addCharacteristic(gattService: BluetoothGattService?, characteristic: BluetoothGattCharacteristic?) {
        gattService ?: return
        characteristic ?: return
        gattService.addCharacteristic(characteristic)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun addService(bluetoothGateServer: BluetoothGattServer?, gattService: BluetoothGattService?) {
        bluetoothGateServer ?: return
        gattService ?: return
        bluetoothGateServer.addService(gattService)
    }
}