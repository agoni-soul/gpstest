package com.soul.main

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
        Thread {
            val sharedPreferences = context.getSharedPreferences("haha", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("age", 25)
            // editor.commit()
            editor.apply()

            editor?.let {

            }
        }.start()
    }
}