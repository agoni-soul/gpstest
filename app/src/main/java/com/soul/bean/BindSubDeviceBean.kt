package com.soul.bean


/**
 *     author : yangzy33
 *     time   : 2023/6/27
 *     desc   :
 *     version: 1.0
 */
/**
 *
 * "subDevices":
 * "[{\"enterpriseCode\":\"0000\",
 * \"modelId\":\"midea.switch.011.003\",
 * \"errorCode\":0,
 * \"subType\":\"1104\",
 * \"sn\":\"9035EAFFFE842AEC\",
 * \"type\":\"0x21\",
 * \"spid\":10001698,
 * \"deviceId\":177021372099829,
 * \"deviceName\":\"midea\",
 * \"errorMsg\":null}]",
 */
data class BindSubDeviceBean(
    val enterpriseCode: String?,
    var modelId: String?,
    var errorCode: String?,
    var subType: String?,
    var sn: String?,
    private var type: String?,
    var spid: String?,
    var deviceId: String?,
    var deviceName: String?,
    var errorMsg: String?
) {
    var name: String? = null

    var sn8: String? = sn?.substring(9, 17)

    fun getType(): String? {
        if (type?.startsWith("0x") == true) {
            return type!!.substring(2)
        }
        return type
    }

    fun setType(type: String?) {
        this.type = type
    }

    override fun toString(): String {
        return "enterpriseCode: $enterpriseCode" +
                "modelId: $modelId, errorCode: $errorCode" +
                "subType: $subType, sn: $sn" +
                "type: $type, spid: $spid" +
                "deviceId: $deviceId, deviceName: $deviceName" +
                "errorMsg: $errorMsg"
    }
}