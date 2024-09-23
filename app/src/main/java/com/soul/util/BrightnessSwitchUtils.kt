package com.soul.util

import android.content.Context
import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings
import java.lang.reflect.InvocationTargetException

/**
 *
 * @author : haha
 * @date   : 2024-09-23
 * @desc   : 调节屏幕亮度的监听
 *
 */
class BrightnessSwitchUtils {
    private var mContext: Context? = null
    private var mPowerManager: PowerManager? = null
    private var mBrightObserver: BrightObserver? = null

    fun init(context: Context?, handler: Handler, actionFun: (Boolean) -> Unit) {
        context ?: return
        mContext = context
        mPowerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        mBrightObserver = BrightObserver(context, handler, actionFun)
    }

    fun startAutoBrightness(contentResolver: ContentResolver?) {
        contentResolver ?: return
        Settings.System.putInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        )
    }

    fun stopAutoBrightness(contentResolver: ContentResolver?) {
        contentResolver ?: return
        Settings.System.putInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
    }

    fun setScreenLightValue(contentResolver: ContentResolver?, value: Int) {
        contentResolver ?: return
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, value)
    }

    fun setLight(light: Int) {
        mPowerManager ?: return
        try {
            val pmClass = Class.forName(mPowerManager!!.javaClass.name)
            val field = pmClass.getDeclaredField("mService")
            field.isAccessible = true
            val iPM = field.get(mPowerManager)
            val iPMClass = Class.forName(iPM.javaClass.name)
            val method = iPMClass.getDeclaredMethod("setBacklightBrightness",
                Int.Companion::class.java
            )
            method.isAccessible = true
            method.invoke(iPM, light)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchFileException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    class BrightObserver(context: Context, handler: Handler, actionFun: ((Boolean) -> Unit)) :
        ContentObserver(handler) {
        private val mResolver: ContentResolver by lazy { context.contentResolver }
        private val mActionFun = actionFun

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            mActionFun.invoke(selfChange)
        }

        fun startObserver() {
            mResolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                false,
                this
            )
            mResolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                false,
                this
            )
        }

        fun stopObserver() {
            mResolver.unregisterContentObserver(this)
        }
    }
}