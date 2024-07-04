package com.soul.receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData


/**
 *     author : yangzy33
 *     time   : 2024-07-04
 *     desc   :
 *     version: 1.0
 */
class DownloadReceiver: BroadcastReceiver() {
    val downloadFileMapLiveData = MutableLiveData<MutableMap<Long, Int>>(mutableMapOf())

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        var progress = 0
        downloadFileMapLiveData.value?.put(downloadId, progress)
        DownloadManager.ACTION_DOWNLOAD_COMPLETE
    }

    // registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
}