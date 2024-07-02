package com.soul.volume

import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


/**
 *     author : yangzy33
 *     time   : 2024-07-01
 *     desc   :
 *     version: 1.0
 */
class DownloadLrc {
    private val TAG: String = this.javaClass.simpleName

    fun writeContentFromUrl(urlPath:String, lrcPath: String): Boolean {
        Log.i(TAG, "lrcURL = $urlPath");
        try {
            val url = URL(urlPath)
            val urlConnection = url.openConnection()
            urlConnection.connect()
            val httpConn = urlConnection as HttpURLConnection
            if (httpConn.responseCode == HttpURLConnection.HTTP_OK) {
                val file = File(lrcPath)
                if (!file.exists()) {
                    file.mkdirs();
                }
                val bf = BufferedReader(InputStreamReader (urlConnection.getInputStream(), "utf-8"))
                val out = PrintWriter(BufferedWriter (OutputStreamWriter (FileOutputStream (lrcPath), "utf-8")))
                val c = charArrayOf()
                var temp = bf.read()
                while (temp != -1) {
                    bf.read(c)
                    out.write(c)
                    temp = bf.read()
                }
                bf.close()
                out.close()
                return true
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    fun getLrcPath(title: String, artist: String): String {
        return "$artist-$title.lrc"
    }

}