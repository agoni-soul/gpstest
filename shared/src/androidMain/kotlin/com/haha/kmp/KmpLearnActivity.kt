package com.haha.kmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/**
 * Android 宿主：只负责把 [KmpLearnApp] 挂到窗口上。
 * 从 [com.haha.main.MainActivity] 点击「KMP / CMP 框架」进入。
 */
class KmpLearnActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KmpLearnApp()
        }
    }
}
