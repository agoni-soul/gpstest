package com.haha.kmp

import platform.UIKit.UIDevice

class IosPlatform : Platform {
    override val name: String = "iOS"
    override val details: String =
        "${UIDevice.currentDevice.systemName} ${UIDevice.currentDevice.systemVersion}"
}

actual fun getPlatform(): Platform = IosPlatform()
