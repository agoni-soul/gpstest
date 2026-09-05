package com.haha.splash

/**
 * 进程级开屏状态：热启动 / 从落地页返回不再出第二次。
 */
object SplashAdSession {
    @Volatile
    var dismissed: Boolean = false
}
