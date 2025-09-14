// CartViewModel.kt
package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.colfi.data.model.CartItem
import com.example.colfi.data.repository.CartRepository
import com.example.colfi.ui.state.CartUiState

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        updateCartSummary()
    }

    fun addToCart(cartItem: CartItem) {
        val currentItems = _uiState.value.cartItems.toMutableList()

        // Check if item with same options already exists
        val existingItemIndex = currentItems.indexOfFirst {
            it.menuItem.id == cartItem.menuItem.id && it.options == cartItem.options
        }

        if (existingItemIndex >= 0) {
            // Update quantity of existing item
            val existingItem = currentItems[existingItemIndex]
            currentItems[existingItemIndex] = existingItem.copy(
                quantity = existingItem.quantity + cartItem.quantity
            )
        } else {
            // Add new item
            currentItems.add(cartItem)
        }

        _uiState.value = _uiState.value.copy(cartItems = currentItems)
        updateCartSummary()
    }

    fun updateCartItemQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(cartItem)
            return
        }

        val currentItems = _uiState.value.cartItems.toMutableList()
        val itemIndex = currentItems.indexOf(cartItem)

        if (itemIndex >= 0) {
            currentItems[itemIndex] = cartItem.copy(quantity = newQuantity)
            _uiState.value = _uiState.value.copy(cartItems = currentItems)
            updateCartSummary()
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        val currentItems = _uiState.value.cartItems.toMutableList()
        currentItems.remove(cartItem)
        _uiState.value = _uiState.value.copy(cartItems = currentItems)
        updateCartSummary()
    }

    fun clearCart() {
        _uiState.value = _uiState.value.copy(cartItems = emptyList())
        updateCartSummary()
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }

    private fun updateCartSummary() {
        val items = _uiState.value.cartItems
        val itemCount = items.sumOf { it.quantity }
        val totalPrice = items.sumOf { it.totalPrice }

        _uiState.value = _uiState.value.copy(
            itemCount = itemCount,
            totalPrice = totalPrice
        )
    }
}