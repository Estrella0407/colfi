package com.example.colfi.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.ui.state.RegisterUiState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun onUsernameChange(newUsername: String) {
        _uiState.value = _uiState.value.copy(username = newUsername)
    }

    fun onEmailChange(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.value = _uiState.value.copy(password = newPassword)
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = newConfirmPassword)
    }

    // Add method to set role
    fun setRole(role: String) {
        _uiState.value = _uiState.value.copy(role = role)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
        )
    }

    fun registerUser() {
        val state = _uiState.value

        if (state.username.isBlank() || state.email.isBlank() ||
            state.password.isBlank() || state.confirmPassword.isBlank()
        ) {
            _uiState.value = state.copy(errorMessage = "All fields are required")
            return
        }

        if (state.role.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Role must be selected")
            return
        }

        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(errorMessage = "Passwords do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            val result = authRepository.registerUser(
                username = state.username,
                email = state.email,
                password = state.password,
                displayName = state.username,
                role = state.role // Pass role to repository
            )

            if (result.isSuccess) {
                val firebaseUser = result.getOrNull()
                val userId = firebaseUser?.uid

                // ✅ Only create wallet for customer role
                if (state.role == "customer" && userId != null) {
                    val db = FirebaseFirestore.getInstance()
                    val walletData = hashMapOf(
                        "balance" to 0.0
                    )
                    db.collection("wallets").document(userId).set(walletData)
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Registration successful!",
                    errorMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }


    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}