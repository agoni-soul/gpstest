package com.haha.volume.lrc

import com.haha.volume.lrc.LrcRow


/**
 *     author : yangzy33
 *     time   : 2024-06-24
 *     desc   :
 *     version: 1.0
 */
interface ILrcBuilder {
    fun getLrcRows(rawLrc: String?): MutableList<LrcRow>?
}