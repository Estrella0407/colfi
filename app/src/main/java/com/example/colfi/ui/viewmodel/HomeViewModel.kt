// HomeViewModel.kt
package com.example.colfi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.Customer
import com.example.colfi.data.model.Guest
import com.example.colfi.data.model.User
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.ui.state.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val authRepository: AuthRepository = AuthRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val quotes = listOf(
        "The perfect cup awaits",
        "Where coffee meets passion",
        "Brewing moments of joy",
        "Every sip tells a story",
        "Fall in Love, One Sip at a Time."
    )

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            randomQuote = quotes.random(),
            errorMessage = null,
            shouldNavigateToLogin = false
        )

        viewModelScope.launch {
            val result = authRepository.getCurrentUser()

            result.fold(
                onSuccess = { user ->
                    when (user) {
                        is Customer -> {
                            _uiState.value = _uiState.value.copy(
                                customer = user,
                                guest = null,
                                user = user as User,
                                isGuest = false,
                                isLoading = false,
                                errorMessage = null,
                                shouldNavigateToLogin = false
                            )
                        }
                        is Guest -> {
                            _uiState.value = _uiState.value.copy(
                                customer = null,
                                guest = user,
                                user = user as User,
                                isGuest = true,
                                isLoading = false,
                                errorMessage = null,
                                shouldNavigateToLogin = false
                            )
                        }
                        else -> {
                            // Staff or other user types shouldn't access customer home
                            // Navigate to login instead of showing error
                            _uiState.value = _uiState.value.copy(
                                customer = null,
                                guest = null,
                                user = null,
                                isGuest = false,
                                isLoading = false,
                                errorMessage = null,
                                shouldNavigateToLogin = true
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    // FIXED: Navigate to login instead of showing error
                    if (exception.message?.contains("No user logged in") == true ||
                        exception.message?.contains("User document not found") == true ||
                        exception.message?.contains("Access denied") == true) {

                        _uiState.value = _uiState.value.copy(
                            customer = null,
                            guest = null,
                            user = null,
                            isGuest = false,
                            isLoading = false,
                            errorMessage = null,
                            shouldNavigateToLogin = true

                        )
                    } else {
                        // For other errors, show error with retry
                        _uiState.value = _uiState.value.copy(
                            customer = null,
                            guest = null,
                            user = null,
                            isGuest = false,
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to load user data",
                            shouldNavigateToLogin = false
                        )
                    }
                }
            )
        }
    }

    // Method to refresh user data (for pull-to-refresh or manual refresh)
    fun refreshUserData() {
        loadCurrentUser()
    }

    // Method to handle guest login
    fun continueAsGuest() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = authRepository.loginAsGuest()

            result.fold(
                onSuccess = { guest ->
                    _uiState.value = _uiState.value.copy(
                        customer = null,
                        guest = guest,
                        user = guest as User,
                        isGuest = true,
                        isLoading = false,
                        errorMessage = null,
                        shouldNavigateToLogin = false
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to continue as guest",
                        shouldNavigateToLogin = false
                    )
                }
            )
        }
    }

    // Get display name for UI
    fun getDisplayName(): String {
        return _uiState.value.customer?.displayName
            ?: _uiState.value.guest?.displayName
            ?: "User"
    }

    // Get wallet balance (only for customers)
    fun getWalletBalance(): Double {
        return _uiState.value.customer?.walletBalance ?: 0.0
    }

    // Get loyalty points (only for customers)
    fun getLoyaltyPoints(): Int {
        return _uiState.value.customer?.points ?: 0
    }

    // Get user tier (only for customers)
    fun getUserTier(): Int {
        return _uiState.value.customer?.tier ?: 0
    }

    // Check if user has enough balance for a purchase
    fun hasEnoughBalance(amount: Double): Boolean {
        return getWalletBalance() >= amount
    }

    // Clear error message
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Handle navigation to login
    fun onNavigateToLoginHandled() {
        _uiState.value = _uiState.value.copy(shouldNavigateToLogin = false)
    }
}