package com.soul.mviFrame.data

import com.soul.mviFrame.base.IMviIntent

sealed interface DataIntent : IMviIntent {
    data class RequestData(val id: String) : DataIntent
    data class RequestGuideInfo(val sn8: String) : DataIntent
}
