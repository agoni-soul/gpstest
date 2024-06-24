package com.soul.volume


/**
 *     author : yangzy33
 *     time   : 2024-06-24
 *     desc   :
 *     version: 1.0
 */
interface ILrcBuilder {
    fun getLrcRows(rawLrc: String?): MutableList<LrcRow>?
}