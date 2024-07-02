package com.soul.volume.media


/**
 *     author : yangzy33
 *     time   : 2024-07-02
 *     desc   :
 *     version: 1.0
 */
enum class PlayMode {
    /**
     * 顺序播放
     */
    PLAY_MODE_SEQUENTIAL,

    /**
     * 单曲循环
     */
    PLAY_MODE_LOOP,

    /**
     * 随机播放
     */
    PLAY_MODE_SHUFFLE;

    override fun toString(): String {
        return name
    }
}