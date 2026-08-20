package com.haha.mviFrame.main

import com.haha.mviFrame.base.IMviUiEffect
import com.haha.mviFrame.base.IMviUiState

data class MainUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList()
) : IMviUiState

sealed interface MainUiEffect : IMviUiEffect {
    data class ShowToast(val message: String) : MainUiEffect
}
