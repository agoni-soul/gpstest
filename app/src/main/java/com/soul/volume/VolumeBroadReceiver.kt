package com.soul.volume

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class VolumeBroadReceiver: BroadcastReceiver() {
    companion object {
        const val VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION"
    }

    private var mCallback: VolumeCallback? = null

    fun setCallback(callback:VolumeCallback?) {
        mCallback = callback
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (VOLUME_CHANGED_ACTION == intent.action) {
            mCallback?.handleVolumeChange()
        }
    }

    interface VolumeCallback {
        fun handleVolumeChange()
    }
}