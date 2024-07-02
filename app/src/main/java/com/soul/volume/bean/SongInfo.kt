package com.soul.volume.bean

import java.io.Serializable


/**
 *     author : yangzy33
 *     time   : 2024-06-21
 *     desc   :
 *     version: 1.0
 */
data class SongInfo(
    val songName: String,
    val singer: String?,
    val songUrl: String,
    var songFileName: String? = null,
    val lrcUrl: String? = null,
    var lrcFileName: String? = null,
) : Cloneable, Serializable {

    @Throws(CloneNotSupportedException::class)
    override fun clone(): SongInfo {
        return super.clone() as SongInfo
    }

    override fun toString(): String {
        return "(songName: $songName, singer: $singer, songUrl: $songUrl, " +
                "songFileName: $songFileName, lrcUrl: $lrcUrl, lrcFileName: $lrcFileName)"
    }
}
