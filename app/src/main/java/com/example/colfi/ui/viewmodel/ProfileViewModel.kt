package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.User
import com.example.colfi.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = authRepository.getCurrentUser()
                _user.value = currentUser
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load user"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout(onNavigateToLogin: () -> Unit) {
        authRepository.logout()
        onNavigateToLogin()
    }
}
