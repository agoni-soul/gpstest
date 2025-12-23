package com.soul.mviFrame.main

/**
 * @auther: soulagoni
 * @Date:   2025/12/7
 * @Detail:
 */
sealed class MainState {

    object Idle : MainState()
    object Loading : MainState()
    data class Users(val user: List<User>) : MainState()
    data class Error(val error: String?) : MainState()

}