package com.soul.volume

import android.Manifest
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.soul.util.PermissionUtils
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
object CacheFile {
    private val TAG: String = javaClass.simpleName

    fun downloadSongInfo(context: Context?, songInfo: SongInfo?) {
        if (context == null || songInfo == null) return
        if (!songInfo.lrcUrl.isNullOrBlank()) {
            val fileName = "songLrcCache"
            downloadFile(context, songInfo.lrcUrl, songInfo.lrcFileName, fileName) { isLocalExist, fileBytes ->
                if (isLocalExist || fileBytes == null) return@downloadFile
                try {
                    val songLrcDir = File(context.externalCacheDir, fileName)
                    songInfo.lrcFileName = "${songInfo.singer}-${songInfo.songName}.lrc"
                    val file = File(songLrcDir, songInfo.lrcFileName!!)
                    val fos = FileOutputStream(file)
                    fos.write(fileBytes)
                    fos.close()
                } catch (e: IOException) {
                    songInfo.lrcFileName = null
                    e.printStackTrace()
                    Log.e(TAG, "lrcUrl: e.msg = ${e.message}")
                }
            }
        }
        if (songInfo.songUrl.isNotBlank()) {
            val fileName = "SongCache"
            downloadFile(context, songInfo.songUrl, songInfo.songFileName, fileName) {isLocalExist, fileBytes ->
                if (isLocalExist || fileBytes == null) return@downloadFile
                try {
                    val songDir = File(context.externalCacheDir, fileName)
                    songInfo.songFileName = "${songInfo.singer}-${songInfo.songName}.mp3"
                    val file = File(songDir, songInfo.songFileName!!)
                    val fos = FileOutputStream(file)
                    fos.write(fileBytes)
                    fos.close()
                } catch (e: IOException) {
                    songInfo.songFileName = null
                    e.printStackTrace()
                    Log.e(TAG, "songUrl: e.msg = ${e.message}")
                }
            }
        }
    }

    fun downloadFile(context: Context, url: String?, urlFileName: String?, cacheFolder:String, dataCallback: (Boolean, ByteArray?) -> Unit){
        if (!PermissionUtils.checkSinglePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(context, "没有写权限", Toast.LENGTH_SHORT).show()
        }
        var isFileExist = false
        val fileDir = File(context.externalCacheDir, cacheFolder)
        Log.d(TAG, "fileDir isExist() = ${fileDir.exists()}")
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        } else {
            val folder = File("${context.externalCacheDir}//${cacheFolder}")
            Log.d(TAG, "folder = $folder")
            if (folder.exists() && folder.isDirectory && !urlFileName.isNullOrBlank()) {
                val files = folder.listFiles()
                if (!files.isNullOrEmpty()) {
                    for (file in files) {
                        Log.d(TAG, "file name = ${file.name}")
                        if (file.name == urlFileName) {
                            dataCallback.invoke(true, file.readBytes())
                            return
                        }
                    }
                }
            }
        }

        if (url.isNullOrBlank()) {
            dataCallback.invoke(false, null)
            return
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                dataCallback.invoke(false, response.body?.bytes())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "ems = ${e.message}")
        }
    }
}