package com.haha.storage

import android.app.Application
import com.haha.hahalearn.BuildConfig
import com.haha.log.DOFLogUtil
import com.haha.storage.MmkvHolder.default
import com.haha.storage.MmkvHolder.init
import com.tencent.mmkv.MMKV
import com.tencent.mmkv.MMKVLogLevel

/**
 * MMKV 入口。启动表 [com.haha.startup.task.MmkvInitTask] 调用 [init] 后可用 [default]。
 */
object MmkvHolder {
    private const val TAG = "MmkvHolder"

    fun init(app: Application) {
        val root = MMKV.initialize(app)
        MMKV.setLogLevel(
            if (BuildConfig.DEBUG) MMKVLogLevel.LevelDebug else MMKVLogLevel.LevelError
        )
        DOFLogUtil.d(TAG, "initialized, root=$root")
    }

    fun default(): MMKV = MMKV.defaultMMKV()
}
