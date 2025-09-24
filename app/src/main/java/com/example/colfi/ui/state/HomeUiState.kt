// HomeUiState.kt
package com.example.colfi.ui.state

import com.example.colfi.data.model.Customer
import com.example.colfi.data.model.Guest
import com.example.colfi.data.model.User

data class HomeUiState(
    val customer: Customer? = null,
    val guest: Guest? = null,
    val user: User? = null, // Unified user field for easier access
    val isGuest: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val randomQuote: String = " ",
    val shouldNavigateToLogin: Boolean = false,
)