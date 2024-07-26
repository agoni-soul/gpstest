package com.soul.bean

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.os.ParcelUuid
import okhttp3.internal.and
import okhttp3.internal.toHexString


/**
 *     author : yangzy33
 *     time   : 2024-07-22
 *     desc   :
 *     version: 1.0
 */
data class BleScanResult(
    val name: String? = null,
    val mac: String? = null,
    val type: Int = 0,
    var bondState: Int = 0,
    val serviceUuids: List<ParcelUuid> = mutableListOf(),
    val deviceUuids: List<ParcelUuid> = mutableListOf(),
    val isLegacy: Boolean = false,
    var isConnectable: Boolean = false,
    val dataStatus: Int = 0,
    val rssi: Int = 0,
    val scanRecord: ScanRecord? = null,
    var device: BluetoothDevice? = null,
    val dataBytes: ByteArray = ByteArray(0),
) {
    val typeDeviceStr: String = typeDeviceStr(type)
    val typeStr: String = typeStr(type)
    val bondStateStr: String = bondState(bondState)
    val dataStatusStr: String = dataStatus(dataStatus)
    val dataHex: String = byteToHex(dataBytes)
    var dataHexDetail: String? = null
        private set
    val dataDatailList: MutableList<BleScanRecordDetailBean> = dataByteDetail(dataHex)

    private fun typeDeviceStr(type: Int): String {
        return when (type) {
            0 -> {
                "Unknown"
            }
            1 -> {
                "Classic - BR/EDR devices"
            }
            2 -> {
                "Low Energy - LE-only"
            }
            3 -> {
                "Dual Mode - BR/EDR/LE"
            }
            else -> {
                "Unknown"
            }
        }
    }

    private fun typeStr(type: Int): String {
        return when (type) {
            0 -> {
                "DEVICE_TYPE_UNKNOWN"
            }
            1 -> {
                "DEVICE_TYPE_CLASSIC"
            }
            2 -> {
                "DEVICE_TYPE_LE"
            }
            3 -> {
                "DEVICE_TYPE_DUAL"
            }
            else -> {
                "DEVICE_TYPE_UNKNOWN"
            }
        }
    }

    private fun bondState(state: Int): String {
        return when (state) {
            10 -> {
                "BOND_NONE"
            }
            11 -> {
                "BOND_BONDING"
            }
            12 -> {
                "BOND_BONDED"
            }
            else -> {
                "BOND_NONE"
            }
        }
    }

    private fun dataStatus(dataStatus: Int): String {
        return when (dataStatus) {
            ScanResult.DATA_COMPLETE -> {
                "DATA_COMPLETE"
            }
            ScanResult.DATA_TRUNCATED -> {
                "DATA_TRUNCATED"
            }
            else -> {
                ""
            }
        }
    }

    private fun byteToHex(dataBytes: ByteArray): String {
        val sb = StringBuilder()
        for (byte in dataBytes) {
            val s = byte.and((0xff))
            val hexS = s.toHexString().uppercase()
            sb.append(if (hexS.length == 1) "0$hexS" else hexS)
        }
        return sb.toString()
    }

    override fun toString(): String {
        val serviceUuidsSb = StringBuilder()
        for (i in serviceUuids.indices) {
            if (i == 0) {
                serviceUuidsSb.append("[")
            }
            serviceUuidsSb.append(serviceUuids[i].uuid)
            if (i == serviceUuids.size - 1) {
                serviceUuidsSb.append("]")
            }
        }
        return "[\nname: $name, mac: $mac, \n" +
                "type: $typeDeviceStr, bondState: $bondStateStr, \n" +
                "serviceUuids: $serviceUuidsSb, isLegacy: $isLegacy, isConnectable: $isConnectable, \n" +
                "dataStatus: $dataStatusStr, rssi: $rssi, \n " +
                "scanRecord: $scanRecord, \n" +
                "device: $device, \n" +
                "bytes: 0x$dataHex, \n" +
                "$dataDatailList\n}]"
    }

    private fun hexStrToInt(hexStr: String): Int {
        var value = 0
        for (c in hexStr) {
            if (c in '0'..'9') {
                value = value * 16 + (c - '0')
            } else {
                when (c) {
                    'a', 'A' -> {
                        value = value * 16 + 10
                    }
                    'b', 'B' -> {
                        value = value * 16 + 11
                    }
                    'c', 'C' -> {
                        value = value * 16 + 12
                    }
                    'd', 'D' -> {
                        value = value * 16 + 13
                    }
                    'e', 'E' -> {
                        value = value * 16 + 14
                    }
                    'f', 'F' -> {
                        value = value * 16 + 15
                    }
                    else -> {
                        return 0
                    }
                }
            }
        }
        return value
    }

    private fun dataByteDetail(dataHex: String): MutableList<BleScanRecordDetailBean> {
        val detailBeanList = mutableListOf<BleScanRecordDetailBean>()
        val dataByteDetailSb = StringBuilder()
        dataByteDetailSb.append("LEN\t\tTYPE\t\tVALUE\n")
        var i = 0
        val length = dataHex.length
        while (i < length) {
            val len: String
            if (i + 1 < length) {
                len = dataHex.substring(i, i + 2)
                i += 2
            } else {
                len = dataHex.substring(i , length)
                i = length
            }
            val lenValue = hexStrToInt(len)
            val type: String
            val value: String
            if (lenValue == 0) {
                break
            } else if ((i + lenValue * 2) < length) {
                type = "0x${dataHex.substring(i, i + 2)}"
                value = dataHex.substring(i + 2, i + lenValue * 2)
                i += (lenValue * 2)
            } else {
                type = "0x${dataHex.substring(i, i + 2)}"
                value = dataHex.substring(i + 2, length)
                i = length
            }
            dataByteDetailSb.append("$lenValue\t$type\t$value\n")
            detailBeanList.add(BleScanRecordDetailBean(lenValue, type, value))
        }
        dataHexDetail = dataByteDetailSb.toString()
        return detailBeanList
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BleScanResult) return false
        return this.mac == other.mac
    }
}

data class BleScanRecordDetailBean(
    val len: Int,
    /**
     * 0x01: Flags(蓝牙的特性)
     * 0x0A: Tx Power Level(发射功率等级)
     * 0xFF: Manufacturer Specific Data(制造商特定数据), 可以获取低功耗蓝牙设备的公司识别码，设备的UUID,Major,Minor，Rssi at 1m(广播校验值)在此处解析
     * 0x16: ServiceData(响应包数据),可以获取ServiceData的UUID和附加数据等
     */
    val type: String? = null,
    val value: String? = null,
) {
    override fun toString(): String {
        return "\n[len: $len, type: $type, value: $value]"
    }
}

fun MutableList<Any>.toString(): String {
    if (this.isEmpty()) {
        return this.toString()
    }
    val sb = StringBuilder()
    for (any in this) {
        sb.append(any.toString())
    }
    return sb.toString()
}

fun ScanResult.toBleScanResult(): BleScanResult {
    val ble = this.device ?: return BleScanResult()
    return BleScanResult(
        name = ble.name ?: "",
        ble.address,
        ble.type,
        ble.bondState,
        serviceUuids = this.scanRecord?.serviceUuids ?: mutableListOf(),
        deviceUuids = ble.uuids?.toMutableList() ?: mutableListOf(),
        this.isLegacy,
        this.isConnectable,
        this.dataStatus,
        this.rssi,
        this.scanRecord,
        this.device,
        dataBytes = this.scanRecord?.bytes ?: ByteArray(0)
    )
}

fun BluetoothDevice.toBleScanResult(): BleScanResult {
    val ble = this
    return BleScanResult(
        name = this.name ?: "",
        this.address,
        this.type,
        ble.bondState,
        deviceUuids = this.uuids?.toMutableList() ?: mutableListOf(),
        device = this
    )
}