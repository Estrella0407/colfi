// ProductsUiState.kt
package com.example.colfi.ui.state

import com.example.colfi.data.model.MenuItem

data class ProductsUiState(
    val selectedCategory: String = "All",
    val categories: List<String> = listOf("All", "Coffee", "Non-coffee", "Tea", "Add On"),
    val products: List<MenuItem> = emptyList(),
    val filteredItems: List<MenuItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lowStockItems: List<MenuItem> = emptyList(),
    val showLowStockOnly: Boolean = false
)
