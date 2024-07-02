package com.soul.volume.media


/**
 *     author : yangzy33
 *     time   : 2024-07-01
 *     desc   :
 *     version: 1.0
 */
enum class MediaStatus {
    MEDIA_PLAYER_STATUS_INIT,
    MEDIA_PLAYER_STATUS_PREPARING,
    MEDIA_PLAYER_STATUS_PREPARED,
    MEDIA_PLAYER_STATUS_START,
    MEDIA_PLAYER_STATUS_PAUSE,
    MEDIA_PLAYER_STATUS_STOP,
    MEDIA_PLAYER_STATUS_COMPLETE,
    MEDIA_PLAYER_STATUS_ERROR,
    MEDIA_PLAYER_STATUS_END;

    override fun toString(): String {
        return name
    }
}