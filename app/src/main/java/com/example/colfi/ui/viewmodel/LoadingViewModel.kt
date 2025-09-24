// LoadingViewModel.kt - Fixed version
package com.example.colfi.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.ui.state.LoadingUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoadingViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(LoadingUiState())
    val uiState: StateFlow<LoadingUiState> = _uiState.asStateFlow()

    // FIXED: Updated to match NavGraph expectations
    fun startLoading(
        onNavigateToLogin: () -> Unit,
        onNavigateToHome: (String, String) -> Unit // userName, userRole
    ) {
        viewModelScope.launch {
            // Show loading animation for 2 seconds
            delay(2000)

            // Check authentication state
            checkAuthState(
                onUserLoggedIn = onNavigateToHome,
                onUserNotLoggedIn = onNavigateToLogin
            )
        }
    }

    // Overloaded function to maintain backward compatibility
    fun startLoading(onComplete: () -> Unit) {
        startLoading(
            onNavigateToLogin = onComplete,
            onNavigateToHome = { _, _ -> onComplete() }
        )
    }

    private suspend fun checkAuthState(
        onUserLoggedIn: (String, String) -> Unit, // userName, userRole
        onUserNotLoggedIn: () -> Unit
    ) {
        try {
            // Add a small delay to allow Firebase Auth to restore state
            delay(500)

            // FIXED: Properly check if user is logged in
            if (authRepository.isUserLoggedIn()) {
                Log.d("LoadingViewModel", "User appears to be logged in, fetching user data...")
                val userResult = authRepository.getCurrentUser()

                userResult.fold(
                    onSuccess = { currentUser ->
                        _uiState.value = _uiState.value.copy(isComplete = true)

                        // FIXED: Use displayName and handle null username properly
                        val userName = currentUser.displayName.ifBlank {
                            currentUser.username.ifBlank { "User" }
                        }

                        Log.d("LoadingViewModel", "User logged in: $userName, Role: ${currentUser.role}")
                        onUserLoggedIn(userName, currentUser.role)
                    },
                    onFailure = { exception ->
                        Log.e("LoadingViewModel", "Failed to get user data: ${exception.message}")
                        _uiState.value = _uiState.value.copy(isComplete = true)

                        // Clear any invalid authentication state
                        authRepository.logoutUser()
                        onUserNotLoggedIn()
                    }
                )
            } else {
                Log.d("LoadingViewModel", "No user logged in")
                _uiState.value = _uiState.value.copy(isComplete = true)
                onUserNotLoggedIn()
            }
        } catch (e: Exception) {
            Log.e("LoadingViewModel", "Error checking auth state: ${e.message}")
            _uiState.value = _uiState.value.copy(isComplete = true)

            // On any error, clear auth state and go to login
            authRepository.logoutUser()
            onUserNotLoggedIn()
        }
    }

    // Add method to manually refresh auth state if needed
    fun refreshAuthState(
        onNavigateToLogin: () -> Unit,
        onNavigateToHome: (String, String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isComplete = false)

            checkAuthState(
                onUserLoggedIn = onNavigateToHome,
                onUserNotLoggedIn = onNavigateToLogin
            )
        }
    }
}