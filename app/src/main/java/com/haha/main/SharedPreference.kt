package com.haha.main

import android.content.Context

/**
 *
 * @author:     haha
 * @date:       2025/7/31
 * Description: SharedPreference学习
 *
 **/
object SharedPreference {

    fun test(context: Context) {
        // 子线程只持有 applicationContext，避免短暂持有 Activity 导致泄漏
        val appContext = context.applicationContext
        Thread {
            val sharedPreferences = appContext.getSharedPreferences("haha", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("age", 25)
            // editor.commit()
            editor.apply()

            editor?.let {

            }
        }.start()
    }
}
