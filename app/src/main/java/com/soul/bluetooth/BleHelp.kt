package com.soul.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.tbruyelle.rxpermissions3.RxPermissions
import java.lang.ref.WeakReference
import java.lang.reflect.Method
import java.util.*


/**
 *     author : yangzy33
 *     time   : 2024-07-24
 *     desc   :
 *     version: 1.0
 */
class BleHelp private constructor() {
    companion object {
        private const val LINK_TIME_OUT = 1000
        private const val START_SCAN = 1001
        private const val STOP_SCAN = 1002
        private const val CONNECT_GATT = 1003
        private const val DISCOVER_SERVICES = 1004
        private const val DISCONNECT_GATT = 1005
        private const val CLOSE_GATT = 1006
        private const val SEND_DATA = 1007

        /**
         * @param bytes
         * @return 将二进制转换为十六进制字符输出
         * new byte[]{0b01111111}-->"7F" ;  new byte[]{0x2F}-->"2F"
         */
        private fun binaryToHexString(bytes: ByteArray?): String {
            var result = ""
            if (bytes == null) {
                return result
            }
            var hex = ""
            for (i in bytes.indices) {
                //字节高4位
                hex = "0123456789ABCDEF"[bytes[i].toInt() and 0xF0 shr 4].toString()
                //字节低4位
                hex += "0123456789ABCDEF"[bytes[i].toInt() and 0x0F].toString()
                result += "$hex,"
            }
            return result
        }

        fun getInstance(): BleHelp = SingleInstance.instance
    }

    private val TAG = javaClass.simpleName

    private var weakReference: WeakReference<FragmentActivity>? = null

    //UUID和Mac地址
    private var mServiceUUID: String? = null

    //特征uuid
    private var mReadCharacteristicUUID: String? = null

    //特征uuid
    private var mWriteCharacteristicUUID: String? = null
    private var mMacAddress: String? = null

    //蓝牙设备
    private var mBluetoothDevice: BluetoothDevice? = null

    //蓝牙服务
    private var mBluetoothGatt: BluetoothGatt? = null

    //子线程的HandlerThread，为子线程提供Looper
    private var workHandlerThread: HandlerThread? = null

    //子线程
    private var workHandler: Handler? = null

    //蓝牙读取特征值
    var mReadGattCharacteristic: BluetoothGattCharacteristic? = null

    //蓝牙写出特征值
    var mWriteGattCharacteristic: BluetoothGattCharacteristic? = null

    //调用disConnect()方法后是否需要调用close方法
    private var isDisConnectNeedClose = true

    //Android8.0以上，退到后台或者息屏后，是否还需要扫描（谷歌为省电8.0以上默认关闭）
    private var isAllowSacnHomeSuperM = false

    //默认连接时间25秒
    private var linkTime = 25000
    private var bleCallback: BleCallback? = null

    /**
     * 静态内部类，单例
     */
    private object SingleInstance {
        val instance = BleHelp()
    }

    fun init(activity: FragmentActivity?, bleCallback: BleCallback?) {
        weakReference = WeakReference(activity)
        this.bleCallback = bleCallback
    }

    private fun checkAllUUID(activity: FragmentActivity?): Boolean {
        weakReference = WeakReference(activity)
        if (weakReference?.get() == null) {
            Log.e(TAG, "BleHelp初始化失败：" + "context为空......")
            Toast.makeText(weakReference!!.get(), "蓝牙模块初始化失败，请联系开发商...", Toast.LENGTH_LONG).show()
            return false
        }
        if (bleCallback == null) {
            Log.e(TAG, "BleHelp初始化失败：" + "bleCallback为空......")
            Toast.makeText(weakReference!!.get(), "蓝牙模块初始化失败，请联系开发商...", Toast.LENGTH_LONG).show()
            return false
        }
        if (!enable()) {
            Log.e(TAG, "BleHelp初始化失败：" + "（用户操作）未打开手机蓝牙，蓝牙功能无法使用......")
            Toast.makeText(weakReference!!.get(), "未打开手机蓝牙,蓝牙功能无法使用...", Toast.LENGTH_LONG).show()
            return false
        }
        if (!isOPenGps) {
            Log.e(TAG, "BleHelp初始化失败：" + "（用户操作）GPS未打开，蓝牙功能无法使用...")
            Toast.makeText(weakReference!!.get(), "GPS未打开，蓝牙功能无法使用", Toast.LENGTH_LONG).show()
            return false
        }
        if (!BluetoothAdapter.checkBluetoothAddress(mMacAddress)) {
            Log.e(TAG, "BleHelp初始化失败：" + "不是一个有效的蓝牙MAC地址，蓝牙功能无法使用...")
            Toast.makeText(weakReference!!.get(), "不是一个有效的蓝牙MAC地址，蓝牙功能无法使用", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (mServiceUUID == null) {
            Log.e(TAG, "BleHelp初始化失败：" + "gattServiceUUID为空，蓝牙功能无法使用...")
            Toast.makeText(weakReference!!.get(), "gattServiceUUID为空，蓝牙功能无法使用", Toast.LENGTH_LONG)
                .show()
            return false
        }
        if (mReadCharacteristicUUID == null) {
            Log.e(TAG, "BleHelp初始化失败：" + "mReadCharacteristicUUID为空，蓝牙功能无法使用...")
            Toast.makeText(
                weakReference!!.get(), "mReadCharacteristicUUID为空，蓝牙功能无法使用", Toast.LENGTH_LONG
            ).show()
            return false
        }
        if (mWriteCharacteristicUUID == null) {
            Log.e(TAG, "BleHelp初始化失败：" + "mWriteCharacteristicUUID为空，蓝牙功能无法使用...")
            Toast.makeText(
                weakReference!!.get(), "mWriteCharacteristicUUID为空，蓝牙功能无法使用", Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    fun setMacAndUuids(
        macAddress: String?,
        gattServiceUUID: String?,
        readGattCharacteristicUUID: String?,
        writeGattCharacteristicUUID: String?
    ) {
        mMacAddress = macAddress
        mServiceUUID = gattServiceUUID
        mReadCharacteristicUUID = readGattCharacteristicUUID
        mWriteCharacteristicUUID = writeGattCharacteristicUUID
    }

    fun setLinkTime(linkTime: Int) {
        this.linkTime = linkTime
    }

    fun start() {
        if (!checkAllUUID(weakReference?.get())) return
        initWorkHandler()
        permissionLocation()
    }

    private fun initWorkHandler() {
        if (workHandlerThread == null || workHandlerThread?.isAlive == false) {
            workHandlerThread = HandlerThread("BleWorkHandlerThread")
            workHandlerThread?.start()
        }
        if (workHandler == null) {
            workHandler = object : Handler(workHandlerThread!!.looper) {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    when (msg.what) {
                        LINK_TIME_OUT -> {
                            removeMessages(LINK_TIME_OUT)
                            sendEmptyMessage(STOP_SCAN)
                            val bluetoothLeScanner: BluetoothLeScanner =
                                bluetoothAdapter.bluetoothLeScanner ?: return
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //允许8.0以上退到后台能继续扫描
                                if (isAllowSacnHomeSuperM) { //Android8.0以上退到后台或息屏后是否还要扫描。我们将其默认为false
                                    //doing....
                                    return
                                }
                            }
                            bluetoothLeScanner.startScan(highScanCallback)
                            return
                        }
                        START_SCAN -> {
                            val bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner ?: return
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (isAllowSacnHomeSuperM) {
                                    return
                                }
                            }
                            bluetoothLeScanner.startScan(highScanCallback)
                            return
                        }
                        STOP_SCAN -> {
                            val bluetoothLeScanner: BluetoothLeScanner =
                                bluetoothAdapter.bluetoothLeScanner
                            bluetoothLeScanner.stopScan(highScanCallback)
                            //停止搜索需要一定的时间来完成，建议加以100ms的延时，保证系统能够完全停止搜索蓝牙设备。
                            sleep()
                        }
                        CONNECT_GATT -> {
                            mBluetoothGatt = mBluetoothDevice?.connectGatt(weakReference?.get(), false, bluetoothGattCallback)
                        }
                        DISCOVER_SERVICES -> {
                            mBluetoothGatt?.discoverServices()
                        }
                        DISCONNECT_GATT -> {
                            val isRefreshSuccess = refreshDeviceCache(mBluetoothGatt)
                            if (isRefreshSuccess) {
                                mBluetoothGatt?.disconnect()
                            } else {
                                Log.e(
                                    TAG, "bluetoothGatt断开连接失败：因清除bluetoothGatt缓存失败,故未调用disconnect()方法"
                                )
                            }
                        }
                        CLOSE_GATT -> {
                            mBluetoothGatt?.close()
                            mBluetoothGatt = null
                            Log.d(TAG, "bluetoothGatt关闭成功并置为null")
                        }
                        SEND_DATA -> {
                            sendData(msg.obj as ByteArray)
                        }
                    }
                }
            }
        }
    }

    /**
     * 是否手机蓝牙状态
     *
     * @return true 表示处于打开状态，false表示处于关闭状态
     */
    val isEnabled: Boolean
        get() {
            val isEnabled = bluetoothAdapter.isEnabled
            Log.d(TAG, "手机蓝牙是否打开：$isEnabled")
            return isEnabled
        }

    /**
     * 打开手机蓝牙
     *
     * @return true 表示打开成功
     */
    fun enable(): Boolean {
        return if (!bluetoothAdapter.isEnabled) {
            //若未打开手机蓝牙，则会弹出一个系统的是否打开/关闭蓝牙的对话框，禁止或者未处理返回false，允许返回true
            //若已打开手机蓝牙，直接返回true
            val enableState = bluetoothAdapter.enable()
            Log.d(TAG, "（用户操作）手机蓝牙是否打开成功：$enableState")
            enableState
        } else true
    }

    /**
     * 关闭手机蓝牙
     *
     * @return true 表示关闭成功
     */
    fun disable(): Boolean {
        return if (bluetoothAdapter.isEnabled) {
            val disabledState = bluetoothAdapter.disable()
            Log.d(TAG, "（用户操作）手机蓝牙是否关闭成功：$disabledState")
            disabledState
        } else true
    }

    /**
     * 判断是否可以通过Mac地址直连
     * 判断通过Mac地址获取到的Device的name是否为空来确定是否可以直连
     * 该方式不是绝对的，仅供参考，需具体情况具体分析
     */
    private val isDirectConnect: Boolean
        private get() {
            val device = bluetoothAdapter.getRemoteDevice(mMacAddress)
            return if (device.name != null) {
                mBluetoothDevice = null
                mBluetoothDevice = device
                true
            } else false
        }

    /**
     * 断开连接
     *
     * @param isNeedClose 执行mBluetoothGatt.disconnect方法后是否需要执行mBluetoothGatt.close方法
     * 执行
     */
    fun disConnect(isNeedClose: Boolean) {
        if (mBluetoothGatt == null) return
        isDisConnectNeedClose = isNeedClose
        workHandler!!.sendEmptyMessage(DISCONNECT_GATT)
    }

    /**
     * 该方法作为扩展方法，暂时设为private
     * 8.0以上退到后台或者息屏后，在没有停止扫描的情况下是否还能继续扫描，谷歌默认不扫描
     */
    private fun allowScanHomeSuperM(isAllow: Boolean) {
        isAllowSacnHomeSuperM = isAllow
    }

    fun sendDataToDevice(data: ByteArray?) {
        val message = Message()
        message.what = SEND_DATA
        message.obj = data
        workHandler!!.sendMessage(message)
    }

    private fun sendData(data: ByteArray) {
        try {
            if (data.size <= 20) {
                if (mWriteGattCharacteristic == null) {
                    Log.e(TAG, "mWriteGattCharacteristic为空，发送数据失败")
                    return
                }
                if (mBluetoothGatt == null) {
                    Log.e(TAG, "mBluetoothGatt为空，发送数据包失败")
                    return
                }
                mWriteGattCharacteristic!!.value = data
                mBluetoothGatt?.writeCharacteristic(mWriteGattCharacteristic)
            } else {
                Log.i(TAG, "数据包分割")
                val b1 = ByteArray(20)
                val b2 = ByteArray(data.size - 20)
                for (i in 0..19) {
                    b1[i] = data[i]
                }
                for (i in 20 until data.size) {
                    b2[i - 20] = data[i]
                }
                sendData(b1)
                sleep()
                sendData(b2)
                sleep() //防止下一条数据发送过快
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "发送数据包异常$e")
        }
    }

    /**
     * 高版本扫描回调
     * Android 5.0（API 21）(包含)以上的蓝牙回调
     */
    private val highScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.d(TAG, "LeScanCallback-->" + "扫描啊啊" + result.device.address)
            if (mMacAddress == result.device.address) {
                workHandler!!.removeMessages(LINK_TIME_OUT)
                workHandler!!.sendEmptyMessage(STOP_SCAN)
                Log.d(TAG, "LeScanCallback-->" + "蓝牙扫描已找到设备，即将开始连接")
                mBluetoothDevice = null
                mBluetoothDevice = result.device
                workHandler!!.sendEmptyMessage(CONNECT_GATT)
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            Log.d(TAG, "ScanCallback: onBatchScanResults")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "ScanCallback: onScanFailed")
        }
    }

    /**
     * 回调都是在子线程中，不可做更新 UI 操作
     */
    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status)
            Log.d(TAG, "onPhyUpdate")
        }

        override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyRead(gatt, txPhy, rxPhy, status)
            Log.d(TAG, "onPhyRead")
        }

        //status-->操作是否成功，如连接成功这个操作是否成功。会返回异常码
        //newState-->新的连接的状态。共四种：STATE_DISCONNECTED，STATE_CONNECTING，STATE_CONNECTED，STATE_DISCONNECTING
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d(
                            TAG,
                            "BluetoothGattCallback：onConnectionStateChange-->" + "status：" + status + "操作成功；" + " newState：" + newState + " 已断开连接状态"
                        )
                        if (isDisConnectNeedClose) workHandler!!.sendEmptyMessage(CLOSE_GATT)
                    }
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d(
                            TAG,
                            "BluetoothGattCallback：onConnectionStateChange-->" + "status：" + status + "操作成功；" + " newState：" + newState + " 已连接状态，可进行发现服务"
                        )
                        //发现服务
                        workHandler!!.sendEmptyMessage(DISCOVER_SERVICES)
                    }
                }
                return
            }
            Log.e(
                TAG,
                "BluetoothGattCallback：onConnectionStateChange-->" + "status：" + status + "操作失败；" + " newState：" + newState
            )
            if (status == 133) { //需要清除Gatt缓存并断开连接和关闭Gatt，然后重新连接
                gattError133("onConnectionStateChange")
            }
        }

        //发现服务成功后，会触发该回调方法。status：远程设备探索是否成功
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            for (i in gatt.services.indices) {
                Log.d(
                    TAG,
                    "onServicesDiscovered-->" + "status:" + status + "操作成功\t急啊急啊: " + gatt.services[i].uuid
                )
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(
                    TAG, "onServicesDiscovered-->" + "status:" + status + "操作成功"
                )
                //根据指定的服务uuid获取指定的服务
                val gattService = gatt.getService(UUID.fromString(mServiceUUID))
                if (gattService == null) {
                    Log.e(
                        TAG,
                        "onServicesDiscovered-->" + "获取服务指定uuid：" + mServiceUUID + "的BluetoothGattService为空，请联系外设设备开发商确认uuid是否正确"
                    )
                    return
                }
                //根据指定特征值uuid获取指定的特征值一
                mReadGattCharacteristic =
                    gattService.getCharacteristic(UUID.fromString(mReadCharacteristicUUID))
                if (mReadGattCharacteristic == null) {
                    Log.e(
                        TAG,
                        "onServicesDiscovered-->" + "获取指定特征值的uuid：" + mReadCharacteristicUUID + "的BluetoothGattCharacteristic为空，请联系外设设备开发商确认特征值uuid是否正确"
                    )
                    return
                }
                //根据指定特征值uuid获取指定的特征值二
                mWriteGattCharacteristic =
                    gattService.getCharacteristic(UUID.fromString(mWriteCharacteristicUUID))
                if (mWriteGattCharacteristic == null) {
                    Log.e(
                        TAG,
                        "onServicesDiscovered-->" + "获取指定特征值的uuid：" + mReadCharacteristicUUID + "的BluetoothGattCharacteristic为空，请联系外设设备开发商确认特征值uuid是否正确"
                    )
                    return
                }
                //设置特征值通知,即设备的值有变化时会通知该特征值，即回调方法onCharacteristicChanged会有该通知
                mBluetoothGatt?.setCharacteristicNotification(mReadGattCharacteristic, true)
                //获取特征值其对应的通知Descriptor
                val descriptor =
                    mReadGattCharacteristic!!.getDescriptor(UUID.fromString(mReadCharacteristicUUID))
                //写入你需要传递给外设的特征的描述值（即传递给外设的信息）
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                //通过GATT实体类将，特征值写入到外设中。在 onDescriptorWrite 回调里面发送握手
                val isSuccessWriteDescriptor = mBluetoothGatt?.writeDescriptor(descriptor) ?: false
                if (!isSuccessWriteDescriptor) {
                    Log.e(
                        TAG,
                        "onServicesDiscovered-->" + "bluetoothGatt将特征值BluetoothGattDescriptor写入外设失败"
                    )
                }
                //通过Gatt对象读取特定特征（Characteristic）的特征值。从外设读取特征值，这个可有可无，一般远程设备的硬件工程师可能不会给该权限
                val isSuccessReadCharacteristic = mBluetoothGatt?.readCharacteristic(mReadGattCharacteristic) ?: false
                if (!isSuccessReadCharacteristic) {
                    Log.e(
                        TAG,
                        "onServicesDiscovered-->" + "读取外设返回的值的操作失败,无法回调onCharacteristicRead，多半硬件工程师的问题或者没给权限"
                    )
                }
                return
            }
            Log.e(TAG, "onServicesDiscovered-->" + "status:" + status + "操作失败")
            if (status == 133) { //需要清除Gatt缓存并断开连接和关闭Gatt，然后重新连接
                gattError133("onServicesDiscovered")
            }
        }

        //接收到的数据，不一定会回调该方法
        override fun onCharacteristicRead(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
//            mReadGattCharacteristic = characteristic
//            mReadCharacteristicUUID = mReadGattCharacteristic?.uuid?.toString()
            Log.d(
                TAG, "onCharacteristicRead-->" + characteristic.value.toString()
            )
        }

        //发送数据后的回调，可以在此检测发送的数据包是否有异常
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
//            mWriteGattCharacteristic = characteristic
//            mWriteCharacteristicUUID = mWriteGattCharacteristic?.uuid?.toString()
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(
                    TAG, "onCharacteristicWrite:发送数据成功：" + binaryToHexString(characteristic.value)
                )
            } else {
                Log.e(TAG, "onCharacteristicWrite:发送数据失败")
            }
        }

        //设备的值有变化时会主动返回
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.d(TAG, "onCharacteristicChanged-->" + characteristic.uuid)
            //过滤，判断是否是目标特征值
            if (mReadCharacteristicUUID != characteristic.uuid.toString()) return
            if (bleCallback != null) bleCallback!!.getDeviceReturnData(characteristic.value)
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)
            Log.d(
                TAG, "onDescriptorRead-->" + "status:" + status + descriptor.uuid
            )
        }

        //设置Descriptor后回调
        override fun onDescriptorWrite(
            gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(
                    TAG, "onDescriptorWrite-->" + "描述符写入操作成功，蓝牙连接成功并可以通信成功！！！" + descriptor.uuid
                )
                if (bleCallback != null) bleCallback!!.connectSuccess()
            } else {
                Log.e(TAG, "onDescriptorWrite-->" + "描述符写入操作失败，蓝牙通信失败...")
            }
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
            super.onReliableWriteCompleted(gatt, status)
            Log.d(TAG, "onReliableWriteCompleted")
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            Log.d(TAG, "onReadRemoteRssi")
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.d(TAG, "onMtuChanged")
        }
    }

    //Gatt操作失败status为133时
    private fun gattError133(method: String) {
        Log.e(
            TAG, "BluetoothGattCallback：$method--> 因status=133，所以将关闭Gatt重新连接..."
        )
        disConnect(true) //断开连接并关闭Gatt
        if (isDirectConnect) {
            Log.d(TAG, "此次为MAC地址直连")
            workHandler!!.sendEmptyMessage(CONNECT_GATT)
        } else {
            Log.d(TAG, "此次为蓝牙扫描连接")
            workHandler!!.sendEmptyMessage(START_SCAN)
        }
    }

    /**
     * 清理本地的BluetoothGatt 的缓存，以保证在蓝牙连接设备的时候，设备的服务、特征是最新的
     */
    private fun refreshDeviceCache(gatt: BluetoothGatt?): Boolean {
        var refreshMethod: Method? = null
        if (null != gatt) {
            try {
                for (methodSub in gatt.javaClass.declaredMethods) {
                    if ("connect".equals(methodSub.name, ignoreCase = true)) {
                        val types = methodSub.parameterTypes
                        if (!types.isNullOrEmpty()) {
                            if ("int".equals(types[0].name, ignoreCase = true)) {
                                refreshMethod = methodSub
                            }
                        }
                    }
                }
                refreshMethod?.invoke(null, null)
                Log.d(TAG, "refreshDeviceCache-->" + "清理本地的BluetoothGatt 的缓存成功")
                return true
            } catch (localException: Exception) {
                localException.printStackTrace()
            }
        }
        Log.e(TAG, "refreshDeviceCache-->" + "清理本地清理本地的BluetoothGatt缓存失败")
        return false
    }//用默认的

    /**
     * 获取BluetoothAdapter，使用默认获取方式。无论如何都不会为空
     */
    private val bluetoothAdapter: BluetoothAdapter
        get() = BluetoothAdapter.getDefaultAdapter() //用默认的

    /**
     * 定位权限
     */
    @SuppressLint("CheckResult")
    private fun permissionLocation() {
        if (weakReference?.get() == null) return
        val rxPermissions = weakReference!!.get()?.let { RxPermissions(it) }
        rxPermissions?.request(Manifest.permission.ACCESS_FINE_LOCATION)?.subscribe { aBoolean ->
                if (aBoolean) {
                    //申请的定位权限允许
                    //设置整个连接过程超时时间
                    workHandler!!.sendEmptyMessageDelayed(
                        LINK_TIME_OUT, linkTime.toLong()
                    )
                    //如果可以Mac直连则不扫描
                    if (isDirectConnect) {
                        Log.d(TAG, "此次为MAC地址直连")
                        workHandler!!.sendEmptyMessage(CONNECT_GATT)
                    } else {
                        Log.d(TAG, "此次为蓝牙扫描连接")
                        workHandler!!.sendEmptyMessage(START_SCAN)
                    }
                } else {
                    //只要有一个权限被拒绝，就会执行
                    Log.d(TAG, "未授权定位权限，蓝牙功能不能使用：")
                    Toast.makeText(weakReference?.get(), "未授权定位权限，蓝牙功能不能使用", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
    // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @return true 表示开启
     */
    private val isOPenGps: Boolean
        get() {
            if (weakReference?.get() == null) return false
            val locationManager = weakReference!!.get()!!
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager
            // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
            val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
            val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (gps || network) {
                Log.d(TAG, "GPS状态：打开")
                return true
            }
            Log.e(TAG, "GPS状态：关闭")
            return false
        }

    /**
     * 睡一下：1.停止扫描时需要调用；2.发送特征值给外设时需要有一定的间隔
     */
    private fun sleep() {
        try {
            Thread.sleep(100) //延时100ms
        } catch (e: InterruptedException) {
            e.printStackTrace()
            Log.i("测试", "延迟异常")
        }
    }

    interface BleCallback {
        fun connectSuccess() //连接成功
        fun getDeviceReturnData(data: ByteArray?)
        fun error(e: Int)
    }
}