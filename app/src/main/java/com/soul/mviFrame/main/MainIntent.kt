package com.soul.mviFrame.main

import com.soul.mviFrame.base.IMviIntent

sealed interface MainIntent : IMviIntent {
    data object FetchUser : MainIntent
}
