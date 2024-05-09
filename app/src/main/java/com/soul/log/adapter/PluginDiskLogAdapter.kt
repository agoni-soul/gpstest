package com.soul.log.adapter

import android.text.TextUtils
import com.soul.log.utils.LogFileUtil

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2023/03/28
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class PluginDiskLogAdapter: DiskLogAdapter {

    companion object {
        /**
         * 插件 日志 常量
         */
        val WEEX_LOG = "weex_log"
        val H5_LOG = "h5_log"
        val PLUGIN_LOG = "plugin_log"
    }

    constructor(filePath: String?):super(filePath) {
        if (filePath == null || TextUtils.isEmpty(filePath)) {
            this.logFilePath = LogFileUtil.getPluginLogDir()
        }
    }

    override fun filter(logType: Int, tag: String?): Boolean {
        return super.filter(logType, tag) && (WEEX_LOG == tag || H5_LOG == tag || PLUGIN_LOG == tag)
    }
}