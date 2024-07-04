package com.soul.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.core.database.getIntOrNull


/**
 *     author : yangzy33
 *     time   : 2024-07-04
 *     desc   :
 *     version: 1.0
 */
object DownloadUtils {

    fun downloadFile(context: Context?, fileUri: String?, downloadStatus: ((Long, Int?) -> Unit)) {
        context ?: return
        fileUri ?: return
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        val uri = Uri.parse(fileUri)
        val request = DownloadManager.Request(uri)
        val downloadId = downloadManager?.enqueue(request) ?: -1
        val cursor = downloadManager?.query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor?.moveToFirst() == true) {
            val status = cursor.getIntOrNull(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            downloadStatus.invoke(downloadId, status)
        }
    }
}