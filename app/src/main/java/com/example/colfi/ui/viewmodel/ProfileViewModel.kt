package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.User
import com.example.colfi.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null // Clear previous errors

            val result = authRepository.getCurrentUser() // Call the correct function

            result.fold(
                onSuccess = { currentUser ->
                    _user.value = currentUser
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load user profile"
                    _user.value = null // Optionally clear user data on failure
                }
            )
            _isLoading.value = false // Set loading to false after completion or error
        }
    }

    fun logout(onNavigateToLogin: () -> Unit) {
        authRepository.logoutUser()
        _user.value = null // Clear user data on logout
        onNavigateToLogin()
    }
}
