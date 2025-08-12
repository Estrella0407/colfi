// HomeUiState.kt
package com.example.colfi.ui.state

import com.example.colfi.data.model.User

data class HomeUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val randomQuote: String = ""
)