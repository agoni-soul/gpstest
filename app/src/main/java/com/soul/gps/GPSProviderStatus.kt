package com.soul.gps

/**
 * 类描述：GPS状态类
 * Created by lizhenya on 2016/9/12.
 */
object GPSProviderStatus {
    //用户手动开启GPS
    const val GPS_ENABLED = 0

    //用户手动关闭GPS
    const val GPS_DISABLED = 1

    //服务已停止，并且在短时间内不会改变
    const val GPS_OUT_OF_SERVICE = 2

    //服务暂时停止，并且在短时间内会恢复
    const val GPS_TEMPORARILY_UNAVAILABLE = 3

    //服务正常有效
    const val GPS_AVAILABLE = 4
}
