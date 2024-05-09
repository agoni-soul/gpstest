package com.soul.bean


/**
 *     author : yangzy33
 *     time   : 2023/6/29
 *     desc   : 搜索子设备，云端推送消息Bean
 *     version: 1.0
 */
/**
 * {"uid":"469f7d2494b94b28ad5ce5edb61ef632",
 * "level":"0",
 * "subDevices":"[{\"enterpriseCode\":\"0000\",\"modelId\":\"midea.switch.011.003\",\"errorCode\":0,\"subType\":\"1104\",\"sn\":\"9035EAFFFE842AEC\",\"type\":\"0x21\",\"spid\":10001698,\"deviceId\":177021372099829,\"deviceName\":\"midea\",\"errorMsg\":null}]",
 * "transId":"DFB2190D5220A53DC35AAF2A1F4FD13B",
 * "appId":"900",
 * "pubTs":"1688024614",
 * "targetUid":"469f7d2494b94b28ad5ce5edb61ef632",
 * "exp":"2023-07-01 15:43:34",
 * "pushTime":"2023-06-29 15:43:34",
 * "gatewayId":"177021372100423",
 * "pushType":"gateway\/subAppliance\/bind"}
 */
data class SubDeviceResultBean(
    val uid: String?,
    val level: String?,
    val subDevices: String?,
    var subDeviceList: List<BindSubDeviceBean>?,
    val transId: String?,
    val appId: String?,
    val pubTs: String?,
    val targetUid: String?,
    val exp: String?,
    val pushTime: String?,
    val gatewayId: String?,
    val pushType: String?
) {
    override fun toString(): String {
        return "uid = $uid, level = $level, " +
                "subDevices = $subDevices, " +
                "subDeviceList = [$subDeviceList], " +
                "transId = $transId, appId = $appId, " +
                "pubTs = $pubTs, targetUid = $targetUid, " +
                "exp = $exp, pushTime = $pushTime, " +
                "gatewayId = $gatewayId, pushType = $pushType, "
    }
}