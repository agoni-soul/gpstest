package com.soul.mviFrame.data

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.soul.mviFrame.base.BaseMVIViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @auther: soulagoni
 * @Date:   2025/12/11
 * @Detail:
 */
class DataMVIViewModel(application: Application): BaseMVIViewModel(application) {

    private val _dataState = MutableStateFlow<DataViewState>(DataViewState())
    val dataState: StateFlow<DataViewState> = _dataState.asStateFlow()
    val dataIntent = Channel<DataIntent>()

    init {
        viewModelScope.launch {
            dataIntent.consumeAsFlow().collect {
                when (it) {
                    is DataIntent.RequestData -> {
                        launch(Dispatchers.IO) {
                            requestData()
                        }
                    }
                    is DataIntent.RequestGuideInfo -> {
                        requestGuideInfo()
                    }
                }
            }
        }
    }

    private suspend fun requestData() {
        _dataState.update { current -> current.copy(isLoading = true)}
        try {
            delay(100)
            _dataState.update { current ->
                current.copy(items = mutableListOf())
            }
        } catch (e: Exception) {
            _dataState.update { current ->
                current.copy(error = e.message)
            }
        }
    }

    private fun requestGuideInfo() {

    }
}