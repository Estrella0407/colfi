// LoginViewModel.kt
package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.ui.state.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            errorMessage = ""
        )
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            errorMessage = ""
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            passwordVisible = !_uiState.value.passwordVisible
        )
    }

    fun login(onSuccess: (String) -> Unit) {
        val currentState = _uiState.value
        if (currentState.username.isEmpty() || currentState.password.isEmpty()) {
            _uiState.value = currentState.copy(errorMessage = "Please fill in all fields")
            return
        }

        _uiState.value = currentState.copy(isLoading = true, errorMessage = "")

        viewModelScope.launch {
            authRepository.login(currentState.username, currentState.password)
                .onSuccess { user ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isLoginSuccessful = true
                    )
                    onSuccess(user.displayName)
                }
                .onFailure { exception ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Login failed"
                    )
                }
        }
    }

    fun loginAsGuest(onSuccess: (String) -> Unit) {
        val guestUser = authRepository.getGuestUser()
        onSuccess(guestUser.displayName)
    }
}