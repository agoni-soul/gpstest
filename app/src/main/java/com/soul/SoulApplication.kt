package com.soul

import android.app.Application


/**
 *     author : yangzy33
 *     time   : 2024-05-17
 *     desc   :
 *     version: 1.0
 */
class SoulApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initComponents()
    }

    private fun initComponents() {
    }
}