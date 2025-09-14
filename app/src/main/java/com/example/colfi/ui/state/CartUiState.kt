// CartUiState.kt
package com.example.colfi.ui.state

import com.example.colfi.data.model.CartItem

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val itemCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)