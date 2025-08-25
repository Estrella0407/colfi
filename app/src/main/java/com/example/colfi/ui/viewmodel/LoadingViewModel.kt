// LoadingViewModel.kt
package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.ui.state.LoadingUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoadingViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(LoadingUiState())
    val uiState: StateFlow<LoadingUiState> = _uiState.asStateFlow()

    fun startLoading(
        onNavigateToLogin: () -> Unit,
        onNavigateToHome: (String) -> Unit
    ) {
        viewModelScope.launch {
            // Show loading animation for 2 seconds
            delay(2000)

            _uiState.value = _uiState.value.copy(isComplete = true)

            // Check if user is already logged in
            checkAuthState(
                onUserLoggedIn = onNavigateToHome,
                onUserNotLoggedIn = onNavigateToLogin
            )
        }
    }

    // Overloaded function to maintain backward compatibility
    fun startLoading(onComplete: () -> Unit) {
        startLoading(onComplete) { }
    }

    private suspend fun checkAuthState(
        onUserLoggedIn: (String) -> Unit,
        onUserNotLoggedIn: () -> Unit
    ) {
        if (authRepository.isUserLoggedIn()) {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                onUserLoggedIn(user.username)
            } else {
                onUserNotLoggedIn()
            }
        } else {
            onUserNotLoggedIn()
        }
    }
}