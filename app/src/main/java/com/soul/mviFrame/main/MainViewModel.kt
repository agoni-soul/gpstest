package com.soul.mviFrame.main

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.soul.mviFrame.base.BaseMVIViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

/**
 * @auther: soulagoni
 * @Date:   2025/12/7
 * @Detail:
 */
class MainViewModel(application: Application) : BaseMVIViewModel(application) {

    private val mService: ApiService by lazy {
        ApiHelperImpl(RetrofitBuilder.apiService)
    }

    private val _sf = MutableStateFlow<MainState>(MainState.Idle)
    val sf: StateFlow<MainState> = _sf

    val userIntent = Channel<MainIntent>(Channel.UNLIMITED)
    private val _state = MutableStateFlow<MainState>(MainState.Idle)
    val state: StateFlow<MainState>
        get() = _state

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            userIntent.consumeAsFlow().collect {
                when (it) {
                    is MainIntent.FetchUser -> fetchUser()
                }
            }
        }
    }

    private fun fetchUser() {
        viewModelScope.launch {
            _state.value = MainState.Loading
            _state.value = try {
                MainState.Users(mService.getUsers())
            } catch (e: Exception) {
                MainState.Error(e.localizedMessage)
            }
        }
    }
}


