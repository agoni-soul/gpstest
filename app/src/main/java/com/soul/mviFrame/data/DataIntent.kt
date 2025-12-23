package com.soul.mviFrame.data

/**
 * @auther: soulagoni
 * @Date:   2025/12/11
 * @Detail:
 */
sealed class DataIntent {
    data class RequestData(val id: String): DataIntent()

    data class RequestGuideInfo(val sn8: String): DataIntent()
}