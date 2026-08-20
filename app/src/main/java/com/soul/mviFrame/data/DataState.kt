package com.soul.mviFrame.data

import com.soul.mviFrame.base.IMviUiEffect
import com.soul.mviFrame.base.IMviUiState
import com.soul.mviFrame.main.User

data class DataUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val items: List<User> = emptyList(),
    val error: String? = null,
    val selectedItem: User? = null,
    val searchQuery: String = ""
) : IMviUiState {
    val filteredItems: List<User>
        get() = if (searchQuery.isBlank()) {
            items
        } else {
            items.filter { it.name.contains(searchQuery, true) }
        }
}

sealed interface DataUiEffect : IMviUiEffect {
    data class ShowToast(val message: String) : DataUiEffect
}
