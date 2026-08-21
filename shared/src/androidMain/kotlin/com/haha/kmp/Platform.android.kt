package com.haha.kmp

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android"
    override val details: String = "API ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})"
}

actual fun getPlatform(): Platform = AndroidPlatform()
