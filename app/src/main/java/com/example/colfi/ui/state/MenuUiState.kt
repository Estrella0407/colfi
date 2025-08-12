// MenuUiState.kt
package com.example.colfi.ui.state

import com.example.colfi.data.model.MenuItem

data class MenuUiState(
    val menuItems: List<MenuItem> = emptyList(),
    val selectedCategory: String = "coffee",
    val categories: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)