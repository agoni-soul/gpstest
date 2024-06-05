package com.soul.util

import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import com.soul.log.DOFLogUtil


/**
 *     author : yangzy33
 *     time   : 2024-06-03
 *     desc   :
 *     version: 1.0
 */
object UnlockUtils {

    private val TAG: String = javaClass.simpleName

    private fun getPowerManager(context: Context?): PowerManager? {
        return context?.getSystemService(Context.POWER_SERVICE) as PowerManager?
    }

    private fun getWakeLock(context: Context?, levelAndFlags: Int, tag: String): PowerManager.WakeLock? {
        return getPowerManager(context)?.newWakeLock(levelAndFlags, tag)
    }

    fun isScreenOn(context: Context?): Boolean {
        val powerManager = getPowerManager(context)
        val isScreenOn = powerManager?.isInteractive ?: false
        DOFLogUtil.d(TAG, "isScreenOn: $isScreenOn")
        return isScreenOn
    }

    fun isLock(context: Context?): Boolean {
        val mKeyguardManager: KeyguardManager? = context?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
        val isLocked =  (mKeyguardManager?.isKeyguardLocked ?: false).and(mKeyguardManager?.isKeyguardSecure ?: false)
        DOFLogUtil.d(TAG, "isLock = $isLocked")
        return isLocked
    }

    fun unlockScreen(context: Context) {
        DOFLogUtil.d(TAG, "unlockScreen")
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
        if (keyguardManager != null) {
            val keyguardLock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE)
            // 解锁屏幕
            keyguardLock.disableKeyguard()
        }
    }


    fun turnScreenOn(context: Context?) {
        DOFLogUtil.d(TAG, "turnScreenOn")
        try {
            val wakeLock = getWakeLock(context, PowerManager.SCREEN_BRIGHT_WAKE_LOCK.or(PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG)
            wakeLock?.acquire(10*60*1000L /*10 minutes*/)
            wakeLock?.release()
        } catch (e: Exception) {
            DOFLogUtil.e(TAG, "turnScreenOn error: ${e.message}")
        }
    }

    fun turnScreenOff(context: Context?, tag: String) {
        try {
            val wakeLock = getWakeLock(context, PowerManager.SCREEN_DIM_WAKE_LOCK, tag)
            wakeLock?.acquire(10*60*1000L /*10 minutes*/)
            wakeLock?.release()
        } catch (e: Exception) {
            DOFLogUtil.e(TAG, "turnScreenOff error: ${e.message}")
        }
    }
}