package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.CartItem
import com.example.colfi.data.repository.CartRepository
import com.example.colfi.data.repository.MenuRepository
import com.example.colfi.ui.state.CartUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {
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
        viewModelScope.launch {
            val result = cartRepository.addToCart(cartItem)
            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = exception.message ?: "Failed to add item to cart"
                )
            }
        }
    }

    fun updateCartItemQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(cartItem)
            return
        }

        viewModelScope.launch {
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
                            cartItem.selectedTemperature,
                            cartItem.selectedSugarLevel
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
        viewModelScope.launch {
            // Find the cart item in the database to get its ID
            val cartItemEntity = cartRepository.getCartItemWithId(
                cartItem.menuItem.id,
                cartItem.selectedTemperature,
                cartItem.selectedSugarLevel
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
        viewModelScope.launch {
            val result = cartRepository.clearCart()
            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = exception.message ?: "Failed to clear cart"
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