// SignUpViewModel.kt
package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.ui.state.SignUpUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            errorMessage = ""
        )
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            errorMessage = ""
        )
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            errorMessage = ""
        )
    }

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username.trim(),
            errorMessage = ""
        )
    }

    fun updateDisplayName(displayName: String) {
        _uiState.value = _uiState.value.copy(
            displayName = displayName.trim(),
            errorMessage = ""
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            passwordVisible = !_uiState.value.passwordVisible
        )
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            confirmPasswordVisible = !_uiState.value.confirmPasswordVisible
        )
    }

    fun signUp(onSuccess: (String) -> Unit) {
        val currentState = _uiState.value

        // Validation
        val validationError = validateSignUpForm(currentState)
        if (validationError != null) {
            _uiState.value = currentState.copy(errorMessage = validationError)
            return
        }

        _uiState.value = currentState.copy(isLoading = true, errorMessage = "")

        viewModelScope.launch {
            authRepository.register(
                email = currentState.email,
                password = currentState.password,
                username = currentState.username,
                displayName = currentState.displayName
            )
                .onSuccess { user ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isSignUpSuccessful = true,
                        errorMessage = ""
                    )
                    onSuccess(user.username)
                }
                .onFailure { exception ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = getSignUpErrorMessage(exception.message)
                    )
                }
        }
    }

    private fun validateSignUpForm(state: SignUpUiState): String? {
        if (state.email.isEmpty() || state.password.isEmpty() ||
            state.confirmPassword.isEmpty() || state.username.isEmpty() ||
            state.displayName.isEmpty()) {
            return "Please fill in all fields"
        }

        if (!isValidEmail(state.email)) {
            return "Please enter a valid email address"
        }

        if (state.password.length < 6) {
            return "Password must be at least 6 characters"
        }

        if (state.password != state.confirmPassword) {
            return "Passwords do not match"
        }

        if (state.username.length < 3) {
            return "Username must be at least 3 characters"
        }

        if (!state.username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            return "Username can only contain letters, numbers, and underscores"
        }

        if (state.displayName.length < 2) {
            return "Display name must be at least 2 characters"
        }

        return null
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun getSignUpErrorMessage(error: String?): String {
        return when {
            error?.contains("email", ignoreCase = true) == true -> "Email already in use"
            error?.contains("username", ignoreCase = true) == true -> "Username already taken"
            error?.contains("weak", ignoreCase = true) == true -> "Password is too weak"
            error?.contains("network", ignoreCase = true) == true -> "Network error. Please check your connection"
            else -> error ?: "Registration failed. Please try again"
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}