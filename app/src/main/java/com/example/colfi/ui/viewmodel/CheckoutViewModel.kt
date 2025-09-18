// CheckoutViewModel.kt
package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.CartItem
import com.example.colfi.data.repository.OrdersRepository
import com.example.colfi.data.repository.CartRepository
import com.example.colfi.ui.state.CheckoutUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val ordersRepository: OrdersRepository = OrdersRepository(),
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun updateCustomerInfo(name: String, phone: String) {
        _uiState.value = _uiState.value.copy(
            customerName = name,
            customerPhone = phone
        )
    }

    fun updateOrderType(orderType: String) {
        _uiState.value = _uiState.value.copy(orderType = orderType)
    }

    fun updatePaymentMethod(paymentMethod: String) {
        _uiState.value = _uiState.value.copy(paymentMethod = paymentMethod)
    }

    fun updateDeliveryAddress(address: String) {
        _uiState.value = _uiState.value.copy(deliveryAddress = address)
    }

    fun updateTableNumber(tableNumber: String) {
        _uiState.value = _uiState.value.copy(tableNumber = tableNumber)
    }

    fun updateSpecialInstructions(instructions: String) {
        _uiState.value = _uiState.value.copy(specialInstructions = instructions)
    }

    fun placeOrder(cartItems: List<CartItem>, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val currentState = _uiState.value

        // Validate required fields
        if (currentState.customerName.isBlank()) {
            onError("Customer name is required")
            return
        }

        if (currentState.customerPhone.isBlank()) {
            onError("Phone number is required")
            return
        }

        if (currentState.orderType.isBlank()) {
            onError("Please select order type")
            return
        }

        if (currentState.paymentMethod.isBlank()) {
            onError("Please select payment method")
            return
        }

        if (currentState.orderType == "delivery" && currentState.deliveryAddress.isBlank()) {
            onError("Delivery address is required")
            return
        }

        if (cartItems.isEmpty()) {
            onError("Cart is empty")
            return
        }

        _uiState.value = currentState.copy(isPlacingOrder = true, errorMessage = "")

        viewModelScope.launch {
            ordersRepository.placeOrder(
                customerName = currentState.customerName,
                customerPhone = currentState.customerPhone,
                cartItems = cartItems,
                orderType = currentState.orderType,
                paymentMethod = currentState.paymentMethod,
                deliveryAddress = currentState.deliveryAddress.takeIf { it.isNotBlank() },
                tableNumber = currentState.tableNumber.takeIf { it.isNotBlank() },
                specialInstructions = currentState.specialInstructions
            ).onSuccess { orderId ->
                _uiState.value = currentState.copy(
                    isPlacingOrder = false,
                    orderPlaced = true
                )
                // Clear cart after successful order
                clearCart()
                onSuccess(orderId)
            }.onFailure { exception ->
                _uiState.value = currentState.copy(
                    isPlacingOrder = false,
                    errorMessage = exception.message ?: "Failed to place order"
                )
                onError(exception.message ?: "Failed to place order")
            }
        }
    }

    private fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }

    fun resetCheckout() {
        _uiState.value = CheckoutUiState()
    }
}