package com.example.colfi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.CartItem
import com.example.colfi.data.model.Customer
import com.example.colfi.data.model.Staff
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.data.repository.CartRepository
import com.example.colfi.data.repository.OrdersRepository
import com.example.colfi.ui.state.CheckoutUiState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val ordersRepository: OrdersRepository = OrdersRepository(),
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository
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

    fun updateSpecialInstructions(instructions: String) {
        _uiState.value = _uiState.value.copy(specialInstructions = instructions)
    }

    fun placeOrder(
        cartItems: List<CartItem>,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentState = _uiState.value

        // Validation
        if (currentState.orderType == "delivery" && currentState.deliveryAddress.isBlank()) {
            onFailure("Delivery address is required"); return
        }
        if (cartItems.isEmpty()) {
            onFailure("Cart is empty"); return
        }

        _uiState.value = currentState.copy(isPlacingOrder = true, errorMessage = "")

        viewModelScope.launch {
            try {
                val totalAmount = cartItems.sumOf { it.menuItem.price * it.quantity }
                val currentUser = FirebaseAuth.getInstance().currentUser

                // Get customerId
                val customerId = if (authRepository.isGuestUser()) {
                    ""
                } else {
                    currentUser?.uid ?: ""
                }

                // IMPORTANT: Wallet payments require authenticated users
                if (currentState.paymentMethod.equals("Wallet", ignoreCase = true)) {
                    if (authRepository.isGuestUser()) {
                        val err = "Please log in to use wallet payments"
                        _uiState.value = currentState.copy(isPlacingOrder = false, errorMessage = err)
                        onFailure(err)
                        return@launch
                    }

                    if (currentUser == null) {
                        val err = "User authentication required for wallet payments"
                        _uiState.value = currentState.copy(isPlacingOrder = false, errorMessage = err)
                        onFailure(err)
                        return@launch
                    }
                }

                // Place order - wallet deduction happens in OrdersRepository
                val orderResult = ordersRepository.placeOrder(
                    customerId = customerId,
                    customerName = currentState.customerName,
                    customerPhone = currentState.customerPhone,
                    cartItems = cartItems,
                    orderType = currentState.orderType,
                    paymentMethod = currentState.paymentMethod,
                    deliveryAddress = currentState.deliveryAddress.takeIf { it.isNotBlank() },
                    specialInstructions = currentState.specialInstructions
                )

                if (orderResult.isSuccess) {
                    val orderId = orderResult.getOrNull().orEmpty()
                    _uiState.value = currentState.copy(isPlacingOrder = false, orderPlaced = true)
                    clearCart()
                    onSuccess(orderId)
                } else {
                    val err = orderResult.exceptionOrNull()?.message ?: "Failed to place order"
                    Log.e("CheckoutViewModel", "Order placement failed: $err")
                    _uiState.value = currentState.copy(isPlacingOrder = false, errorMessage = err)
                    onFailure(err)
                }
            } catch (e: Exception) {
                val err = e.message ?: "Something went wrong"
                _uiState.value = currentState.copy(isPlacingOrder = false, errorMessage = err)
                onFailure(err)
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
