package com.haha.service.impl.utils

import android.app.Application
import android.os.Build
import java.io.File

object ProcessUtils {
    fun currentProcessName(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Application.getProcessName().orEmpty()
        }
        return try {
            File("/proc/self/cmdline").readText().trim { it <= ' ' || it == '\u0000' }
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * [declared] 为空表示所有进程；`:push` 匹配后缀；完整进程名精确匹配。
     */
    fun isCurrentProcess(declared: String): Boolean {
        if (declared.isEmpty()) {
            return true
        }
        val name = currentProcessName()
        if (name.isEmpty()) {
            return true
        }
        return when {
            declared.startsWith(":") -> name.endsWith(declared)
            name == declared -> true
            name.endsWith(":$declared") -> true
            else -> false
        }
    }
}
