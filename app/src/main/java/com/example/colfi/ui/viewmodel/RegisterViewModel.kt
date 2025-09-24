// RegisterViewModel.kt
package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.ui.state.RegisterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onUsernameChange(newUsername: String) {
        _uiState.update { it.copy(username = newUsername, errorMessage = null) }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, errorMessage = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, errorMessage = null) }
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = newConfirmPassword, errorMessage = null) }
    }

    fun setRole(role: String) {
        _uiState.update { it.copy(role = role, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    // FIXED: Proper email validation
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // FIXED: Password strength validation
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6 // Firebase Auth minimum
    }

    fun registerUser() {
        val state = _uiState.value

        // Validation
        when {
            state.username.isBlank() || state.email.isBlank() ||
                    state.password.isBlank() || state.confirmPassword.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "All fields are required") }
                return
            }

            state.role.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Role must be selected") }
                return
            }

            !isValidEmail(state.email) -> {
                _uiState.update { it.copy(errorMessage = "Please enter a valid email address") }
                return
            }

            !isValidPassword(state.password) -> {
                _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters long") }
                return
            }

            state.password != state.confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "Passwords do not match") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authRepository.registerUser(
                username = state.username,
                email = state.email,
                password = state.password,
                displayName = state.username, // You might want to add a separate displayName field
                role = state.role
            )

            _uiState.update {
                if (result.isSuccess) {
                    it.copy(
                        isLoading = false,
                        successMessage = result.getOrNull(),
                        errorMessage = null
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    // FIXED: Reset form after successful registration
    fun resetForm() {
        _uiState.value = RegisterUiState()
    }
}