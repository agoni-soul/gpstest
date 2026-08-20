package com.soul.mviFrame.main

import com.soul.mviFrame.base.IMviUiEffect
import com.soul.mviFrame.base.IMviUiState

data class MainUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList()
) : IMviUiState

sealed interface MainUiEffect : IMviUiEffect {
    data class ShowToast(val message: String) : MainUiEffect
}
