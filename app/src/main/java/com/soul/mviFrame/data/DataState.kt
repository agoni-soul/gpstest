package com.soul.mviFrame.data

import com.soul.mviFrame.main.User

/**
 * @auther: soulagoni
 * @Date:   2025/12/11
 * @Detail:
 */
sealed class DataState {
    object Idle: DataState()
    object Loading: DataState()
    data class Success(val user: User): DataState()
    data class Error(val errorMessage: String): DataState()
}

// ViewState.kt
data class DataViewState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val items: List<User> = emptyList(),
    val error: String? = null,
    val selectedItem: User? = null,
    val searchQuery: String = ""
) {
    // 派生状态
    val filteredItems: List<User>
        get() = if (searchQuery.isBlank()) {
            items
        } else {
            items.filter { it.name.contains(searchQuery, true) }
        }

    // 状态拷贝方法
    fun copyWithLoading(isLoading: Boolean): DataViewState {
        return this.copy(isLoading = isLoading)
    }

    fun copyWithError(error: String?): DataViewState {
        return this.copy(error = error, isLoading = false)
    }
}