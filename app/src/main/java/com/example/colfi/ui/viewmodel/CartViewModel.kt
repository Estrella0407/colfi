// CartViewModel.kt
package com.example.colfi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.CartItem
import com.example.colfi.data.repository.CartRepository
import com.example.colfi.ui.state.CartUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CartViewModel(val cartRepository: CartRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CartUiState(isLoading = true))
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        Log.d("CartViewModel", "INIT block started for instance: $this") // Log ViewModel instance
        viewModelScope.launch {
            Log.d("CartViewModel", "Coroutine launched in init for instance: $this")
            cartRepository.getAllCartItems().collectLatest { itemsFromRepo ->
                Log.d("CartViewModel", "Received ${itemsFromRepo.size} items from repository for instance: $this. Items: $itemsFromRepo")
                _uiState.value = _uiState.value.copy(
                    cartItems = itemsFromRepo,
                    isLoading = false
                )
                updateCartSummaryLocally(itemsFromRepo)
            }
        }
    }

    private fun updateCartSummaryLocally(items: List<CartItem>) {
        val itemCount = items.sumOf { it.quantity }
        val totalPrice = items.sumOf { it.totalPrice }
        Log.d("CartViewModel", "Updating summary: Count=$itemCount, Price=$totalPrice for instance: $this")
        _uiState.value = _uiState.value.copy(
            itemCount = itemCount,
            totalPrice = totalPrice
        )
    }

    fun addToCart(cartItem: CartItem) {
        Log.d("CartViewModel", "addToCart called with ${cartItem.menuItem.name}, Qty: ${cartItem.quantity} for instance: $this")
        viewModelScope.launch {
            val result = cartRepository.addToCart(cartItem)
            if (result.isFailure) {
                val errorMessage = result.exceptionOrNull()?.message ?: "Failed to add item"
                Log.e("CartViewModel", "Failed to add item: $errorMessage for instance: $this")
                _uiState.value = _uiState.value.copy(
                    errorMessage = errorMessage
                )
            } else {
                Log.d("CartViewModel", "addToCart succeeded in repository for instance: $this")
                // Data should flow from the repository via collectLatest in init
            }
        }
    }

    fun updateCartItemQuantity(cartItem: CartItem, newQuantity: Int) {
        Log.d("CartViewModel", "updateCartItemQuantity called for ${cartItem.menuItem.name} to Qty: $newQuantity for instance: $this")
        viewModelScope.launch {

            val itemEntityToUpdate = cartRepository.getCartItemWithId(cartItem.menuItem.id)

            if (itemEntityToUpdate == null) {
                Log.e("CartViewModel", "Item not found in repository for update: ${cartItem.menuItem.name}")
                _uiState.value = _uiState.value.copy(errorMessage = "Item not found for update.")
                return@launch
            }

            if (newQuantity <= 0) {
                Log.d("CartViewModel", "New quantity is <= 0, removing item ID: ${itemEntityToUpdate.id}")
                val removeResult = cartRepository.removeFromCart(itemEntityToUpdate.id) // Assuming id is Long PK
                if (removeResult.isFailure) {
                    val errorMsg = removeResult.exceptionOrNull()?.message ?: "Failed to remove item"
                    Log.e("CartViewModel", "Failed to remove item: $errorMsg")
                    _uiState.value = _uiState.value.copy(errorMessage = errorMsg)
                }
            } else {
                Log.d("CartViewModel", "Updating quantity for item ID: ${itemEntityToUpdate.id} to $newQuantity")
                val updateResult = cartRepository.updateQuantity(itemEntityToUpdate.id, newQuantity) // Assuming id is Long PK
                if (updateResult.isFailure) {
                    val errorMsg = updateResult.exceptionOrNull()?.message ?: "Failed to update quantity"
                    Log.e("CartViewModel", "Failed to update quantity: $errorMsg")
                    _uiState.value = _uiState.value.copy(errorMessage = errorMsg)
                }
            }
            // Data should flow from the repository via collectLatest in init
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        Log.d("CartViewModel", "removeFromCart called for ${cartItem.menuItem.name}")
        viewModelScope.launch {
            val itemEntityToRemove = cartRepository.getCartItemWithId(cartItem.menuItem.id)

            if (itemEntityToRemove == null) {
                Log.e("CartViewModel", "Item not found in repository for removal: ${cartItem.menuItem.name}")
                _uiState.value = _uiState.value.copy(errorMessage = "Item not found for removal.")
                return@launch
            }

            Log.d("CartViewModel", "Removing item ID: ${itemEntityToRemove.id}")
            val result = cartRepository.removeFromCart(itemEntityToRemove.id) // Assuming id is Long PK
            if (result.isFailure) {
                val errorMessage = result.exceptionOrNull()?.message ?: "Failed to remove item"
                Log.e("CartViewModel", "Failed to remove item: $errorMessage for instance: $this")
                _uiState.value = _uiState.value.copy(
                    errorMessage = errorMessage
                )
            }
            // Data should flow from the repository via collectLatest in init
        }
    }

    fun clearCart() {
        Log.d("CartViewModel", "clearCart called for instance: $this")
        viewModelScope.launch {
            val result = cartRepository.clearCart()
            if (result.isFailure) {
                val errorMessage = result.exceptionOrNull()?.message ?: "Failed to clear cart"
                Log.e("CartViewModel", "Failed to clear cart: $errorMessage for instance: $this")
                _uiState.value = _uiState.value.copy(
                    errorMessage = errorMessage
                )
            }
            // Data should flow from the repository via collectLatest in init
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}
