// HomeViewModel.kt
package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.ui.state.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val quotes = listOf(
        "The perfect cup awaits",
        "Where coffee meets passion",
        "Brewing moments of joy",
        "Every sip tells a story",
        "Fall in Love, One Sip at a Time."
    )

    fun initialize(userName: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            randomQuote = quotes.random()
        )

        viewModelScope.launch {
            // If it's a guest user, use guest data
            val user = if (userName == "Guest") {
                authRepository.getGuestUser()
            } else {
                // For demo purposes, we'll create a user based on the name
                // In a real app, you'd fetch this from your backend
                when (userName) {
                    "Admin User" -> authRepository.login("admin", "123456").getOrNull()
                    "Jenny Chen" -> authRepository.login("jenny", "password").getOrNull()
                    else -> authRepository.getGuestUser()
                }
            }

            _uiState.value = _uiState.value.copy(
                user = user,
                isLoading = false
            )
        }
    }
}