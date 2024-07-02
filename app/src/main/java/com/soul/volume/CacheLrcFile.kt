package com.soul.volume

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 *     author : yangzy33
 *     time   : 2024-07-01
 *     desc   :
 *     version: 1.0
 */
object CacheLrcFile {
    private val TAG = "CacheLrcFile"

    fun downloadFile(context: Context, songInfo: SongInfo) {
        val cacheDir = context.cacheDir
        val downloadDir = File(cacheDir, "downloads")
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        if (songInfo.lrcUrl.isNullOrBlank()) return

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(songInfo.lrcUrl)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val file = File(downloadDir, "${songInfo.singer}-${songInfo.songName}.lrc")
                val fos = FileOutputStream(file)
                fos.write(response.body?.bytes())
                fos.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "ems = ${e.message}")
        }
    }
}