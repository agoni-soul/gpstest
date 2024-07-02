package com.soul.volume.media

import android.Manifest
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.soul.util.PermissionUtils
import com.soul.volume.bean.SongInfo
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
        val fileName = "${songInfo.singer}-${songInfo.songName}"
        if (!songInfo.lrcUrl.isNullOrBlank()) {
            val isSuccess = writeFile(context, songInfo.lrcUrl, "songLrcCache", fileName, "lrc")
            if (isSuccess) {
                songInfo.lrcFileName = "$fileName.lrc"
            }
        }
        if (songInfo.songUrl.isNotBlank()) {
            val isSuccess = writeFile(context, songInfo.lrcUrl, "songCache", fileName, "mp3")
            if (isSuccess) {
                songInfo.songFileName = "$fileName.mp3"
            }
        }
        Log.d(TAG, songInfo.toString())
    }

    private fun writeFile(context: Context?, url: String?, folderName: String, lrcFileName: String, suffixName: String): Boolean {
        context ?: return false
        var isSuccessDownload = false
        if (!url.isNullOrBlank()) {
            downloadFile(context, url, lrcFileName, folderName) { isLocalExist, fileBytes ->
                if (isLocalExist || fileBytes == null) return@downloadFile
                try {
                    val songLrcDir = File(context.externalCacheDir, folderName)
                    val file = File(songLrcDir, "$lrcFileName.$suffixName")
                    val fos = FileOutputStream(file)
                    fos.write(fileBytes)
                    fos.close()
                    isSuccessDownload = true
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e(TAG, "writeFile: e.msg = ${e.message}")
                }
            }
        }
        return isSuccessDownload
    }

    fun downloadFile(context: Context, url: String?, urlFileName: String?, cacheFolder:String, dataCallback: (Boolean, ByteArray?) -> Unit){
        if (!PermissionUtils.checkSinglePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(context, "没有写权限", Toast.LENGTH_SHORT).show()
        }
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