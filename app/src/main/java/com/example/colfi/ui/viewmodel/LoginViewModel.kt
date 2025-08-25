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

    // Update to handle email instead of username for Firebase
    fun updateUsername(email: String) {
        _uiState.value = _uiState.value.copy(
            username = email, // This will now store the email
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

        // Basic email validation
        if (!isValidEmail(currentState.username)) {
            _uiState.value = currentState.copy(errorMessage = "Please enter a valid email address")
            return
        }

        _uiState.value = currentState.copy(isLoading = true, errorMessage = "")

        viewModelScope.launch {
            // Use email and password for Firebase authentication
            authRepository.login(currentState.username, currentState.password)
                .onSuccess { user ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isLoginSuccessful = true
                    )
                    onSuccess(user.username) // Pass username for navigation
                }
                .onFailure { exception ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = getFirebaseErrorMessage(exception.message)
                    )
                }
        }
    }

    fun loginAsGuest(onSuccess: (String) -> Unit) {
        val guestUser = authRepository.getGuestUser()
        onSuccess(guestUser.displayName)
    }

    // For demo purposes - create demo accounts in Firebase
    fun loginWithDemo(username: String, onSuccess: (String) -> Unit) {
        val demoCredentials = mapOf(
            "admin" to "admin@colfi.com",
            "jenny" to "jenny@colfi.com"
        )

        val email = demoCredentials[username]
        if (email != null) {
            _uiState.value = _uiState.value.copy(username = email, password = "123456")
            login(onSuccess)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun getFirebaseErrorMessage(error: String?): String {
        return when {
            error?.contains("password") == true -> "Invalid email or password"
            error?.contains("network") == true -> "Network error. Please check your connection"
            error?.contains("user") == true -> "No account found with this email"
            else -> error ?: "Login failed. Please try again"
        }
    }
}