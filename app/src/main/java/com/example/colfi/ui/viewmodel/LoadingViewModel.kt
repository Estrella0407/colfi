// LoadingViewModel.kt
package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.ui.state.LoadingUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoadingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoadingUiState())
    val uiState: StateFlow<LoadingUiState> = _uiState.asStateFlow()

    fun startLoading(onComplete: () -> Unit) {
        viewModelScope.launch {
            delay(3000)
            _uiState.value = _uiState.value.copy(isComplete = true)
            onComplete()
        }
    }
}