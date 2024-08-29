package com.soul.blesdk.interfaces

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import com.soul.blesdk.exceptions.BleErrorException

/**
 *
 * @author haha
 * @date 2024-08-26
 * @version 1.0
 *
 */
abstract class BleGattCallback: BluetoothGattCallback() {
    companion object {
        /**
         * GATT为空
         */
        const val GATT_STATUS_GATT_NULL = 0

        /**
         * 根据GATT获取的服务为空
         */
        const val GATT_STATUS_NO_FIND_SERVICE  = 0
    }
    abstract fun onObtainGattServiceStatus(gatt: BluetoothGatt?, status: Int)

    abstract fun onReadOrWriteException(gatt: BluetoothGatt?, bleException: BleErrorException?)
}