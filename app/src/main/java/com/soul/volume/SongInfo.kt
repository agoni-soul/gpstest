package com.soul.volume


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
)
