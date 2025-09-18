// LoginUiState.kt
package com.example.colfi.ui.state

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isLoginSuccessful: Boolean = false,
    val loggedInUserName: String? = null,
    val loggedInUserRole: String? = null
)