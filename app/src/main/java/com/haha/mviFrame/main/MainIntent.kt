package com.haha.mviFrame.main

import com.haha.mviFrame.base.IMviIntent

sealed interface MainIntent : IMviIntent {
    data object FetchUser : MainIntent
}
