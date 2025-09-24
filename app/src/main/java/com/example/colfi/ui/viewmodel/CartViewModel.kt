// CartViewModel.kt
package com.example.colfi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.CartItem
import com.example.colfi.data.repository.CartRepository
import com.example.colfi.data.repository.MenuRepository
import com.example.colfi.ui.state.CartUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(val cartRepository: CartRepository) : ViewModel() {
    private val menuRepository: MenuRepository = MenuRepository()
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        loadCartItems()
        observeCartSummary()
    }

    private fun loadCartItems() {
        viewModelScope.launch {
            cartRepository.getAllCartItems().collect { cartItems ->
                _uiState.value = _uiState.value.copy(cartItems = cartItems)
            }
        }
    }

    private fun observeCartSummary() {
        viewModelScope.launch {
            cartRepository.getTotalItemCount().collect { itemCount ->
                _uiState.value = _uiState.value.copy(itemCount = itemCount)
            }
        }

        viewModelScope.launch {
            cartRepository.getTotalPrice().collect { totalPrice ->
                _uiState.value = _uiState.value.copy(totalPrice = totalPrice)
            }
        }
    }

    fun addToCart(cartItem: CartItem) {
        Log.d(
            "CartViewModel",
            "addToCart called with ${cartItem.menuItem.name}, Qty: ${cartItem.quantity}, Temp: ${cartItem.selectedTemperature}, Sugar: ${cartItem.selectedSugarLevel} for instance: $this"
        )
        viewModelScope.launch {
            val result = cartRepository.addToCart(cartItem)
            result.onFailure { exception ->
                val errorMessage = exception.message ?: "Failed to add item to cart"
                Log.e("CartViewModel", "Failed to add item: $errorMessage for instance: $this")
                _uiState.value = _uiState.value.copy(
                    errorMessage = errorMessage
                )
            }.onSuccess {
                Log.d("CartViewModel", "addToCart succeeded in repository for instance: $this")
                // Data should flow from the repository via collectLatest in init
            }
        }
    }

    fun updateCartItemQuantity(cartItem: CartItem, newQuantity: Int) {
        Log.d(
            "CartViewModel",
            "updateCartItemQuantity called for ${cartItem.menuItem.name} to Qty: $newQuantity for instance: $this"
        )
        viewModelScope.launch {
            // If quantity is 0 or less, remove the item
            if (newQuantity <= 0) {
                removeFromCart(cartItem)
                return@launch
            }

            // First check if item is available
            menuRepository.checkItemAvailability(
                cartItem.menuItem.category,
                cartItem.menuItem.id,
                newQuantity
            ).fold(
                onSuccess = { isAvailable ->
                    if (isAvailable) {
                        // Find the cart item in the database to get its ID
                        val cartItemEntity = cartRepository.getCartItemWithId(
                            cartItem.menuItem.id,
                            cartItem.selectedTemperature ?: "",
                            cartItem.selectedSugarLevel ?: ""
                        )

                        cartItemEntity?.let { entity ->
                            val result = cartRepository.updateQuantity(entity.id, newQuantity)
                            result.onFailure { exception ->
                                _uiState.value = _uiState.value.copy(
                                    errorMessage = exception.message ?: "Failed to update quantity"
                                )
                            }
                        } ?: run {
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Item not found in cart"
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Not enough ${cartItem.menuItem.name} in stock"
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Failed to check availability"
                    )
                }
            )
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        Log.d("CartViewModel", "removeFromCart called for ${cartItem.menuItem.name}")
        viewModelScope.launch {
            // Find the cart item in the database to get its ID
            val cartItemEntity = cartRepository.getCartItemWithId(
                cartItem.menuItem.id,
                cartItem.selectedTemperature ?: "",
                cartItem.selectedSugarLevel ?: ""
            )

            cartItemEntity?.let { entity ->
                val result = cartRepository.removeFromCart(entity.id)
                result.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Failed to remove item from cart"
                    )
                }
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Item not found in cart"
                )
            }
        }
    }

    fun clearCart() {
        Log.d("CartViewModel", "clearCart called for instance: $this")
        viewModelScope.launch {
            val result = cartRepository.clearCart()
            if (result.isFailure) {
                val errorMessage = result.exceptionOrNull()?.message ?: "Failed to clear cart"
                Log.e(
                    "CartViewModel",
                    "Failed to clear cart: $errorMessage for instance: $this"
                )
                _uiState.value = _uiState.value.copy(
                    errorMessage = errorMessage
                )
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }

    // Helper method to increase quantity by 1
    fun increaseQuantity(cartItem: CartItem) {
        updateCartItemQuantity(cartItem, cartItem.quantity + 1)
    }

    // Helper method to decrease quantity by 1
    fun decreaseQuantity(cartItem: CartItem) {
        updateCartItemQuantity(cartItem, cartItem.quantity - 1)
    }

    // Process order and clear cart
    fun processOrder(): Result<Unit> {
        return try {
            val cartItems = _uiState.value.cartItems
            if (cartItems.isEmpty()) {
                return Result.failure(Exception("Cart is empty"))
            }

            // Process order logic would go here (payment, etc.)
            // After successful order processing:
            clearCart()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}