package com.example.colfi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.ui.state.LoginUiState
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

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

    /**
     * Attempts to log in using the provided username and password via Firebase.
     * The result of the login attempt (success or failure) is reflected in the uiState.
     */
    fun login() {
        val currentState = _uiState.value
        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please fill in all fields") }
            return
        }

        // Set loading state and clear previous errors/success flags
        _uiState.update { it.copy(isLoading = true, errorMessage = "", isLoginSuccessful = false) }

        viewModelScope.launch {
            val result = authRepository.login(currentState.username, currentState.password)

            result.onSuccess { user ->
                // On success, update the state with the user's name and success flag
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        loggedInUserName = user.displayName, // Store the user's name on success
                        loggedInUserRole = user.role // Store the user's role on success
                    )
                }
            }.onFailure { exception ->
                // On failure, update the state with the error message
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Invalid username or password"
                    )
                }
            }
        }
    }

    /**
     * Resets the login success flag. This should be called by the UI after it has
     * handled the navigation, to prevent re-triggering navigation on configuration changes.
     */
    fun onLoginHandled() {
        _uiState.update { it.copy(
            isLoginSuccessful = false,
            loggedInUserName = null,
            loggedInUserRole = null
        ) }
    }

    // --- NEW FUNCTION FOR TRUE ANONYMOUS LOGIN ---
    fun loginAsGuest() {
        // 1. Set loading state and clear previous errors
        _uiState.update { it.copy(
            isLoading = true,
            errorMessage = "",
            isLoginSuccessful = false,
            loggedInUserRole = "guest"
        ) }

        viewModelScope.launch {
            // 2. The ViewModel CALLS the new repository function
            val result = authRepository.loginAsGuest()

            // 3. Handle the result from the repository
            result.onSuccess { guestUser ->
                // On success, update the UI state to trigger navigation
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        loggedInUserName = guestUser.displayName // Will be "Guest"
                    )
                }
            }.onFailure { exception ->
                // On failure, update the UI state with an error message
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