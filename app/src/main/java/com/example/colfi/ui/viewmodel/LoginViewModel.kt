// LoginViewModel.kt
package com.example.colfi.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.ui.state.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val authRepository: AuthRepository = AuthRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateUsername(username: String) {
        _uiState.update { it.copy(username = username, errorMessage = "") }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = "") }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun login() {
        val currentState = _uiState.value
        Log.d("LoginViewModel", "Login attempt - Username: '${currentState.username}', Password length: ${currentState.password.length}")

        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please fill in all fields") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = "", isLoginSuccessful = false) }

        viewModelScope.launch {
            Log.d("LoginViewModel", "Starting authentication...")
            val result = authRepository.login(currentState.username, currentState.password)

            result.onSuccess { user ->
                Log.d("LoginViewModel", "Login successful - User: ${user.displayName}, Role: ${user.role}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        loggedInUserName = user.displayName,
                        loggedInUserRole = user.role
                    )
                }
            }.onFailure { exception ->
                Log.e("LoginViewModel", "Login failed: ${exception.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Invalid username or password"
                    )
                }
            }
        }
    }

    fun onLoginHandled() {
        Log.d("LoginViewModel", "Login handled - clearing success state")
        _uiState.update { it.copy(
            isLoginSuccessful = false,
            loggedInUserName = null,
            loggedInUserRole = null
        ) }
    }

    fun loginAsGuest() {
        Log.d("LoginViewModel", "Guest login attempt")
        _uiState.update { it.copy(
            isLoading = true,
            errorMessage = "",
            isLoginSuccessful = false
        ) }

        viewModelScope.launch {
            val result = authRepository.loginAsGuest()

            result.onSuccess { guestUser ->
                Log.d("LoginViewModel", "Guest login successful - User: ${guestUser.displayName}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        loggedInUserName = guestUser.displayName,
                        loggedInUserRole = guestUser.role
                    )
                }
            }.onFailure { exception ->
                Log.e("LoginViewModel", "Guest login failed: ${exception.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not sign in as guest."
                    )
                }
            }
        }
    }
}