package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.CartItem
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.data.repository.CartRepository
import com.example.colfi.data.repository.OrdersRepository
import com.example.colfi.ui.state.CheckoutUiState
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

    fun updateTableNumber(tableNumber: String) {
        _uiState.value = _uiState.value.copy(tableNumber = tableNumber)
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

        // Validate required fields
        /*if (currentState.customerName.isBlank()) {
            onFailure("Customer name is required"); return
        }
        if (currentState.customerPhone.isBlank()) {
            onFailure("Phone number is required"); return
        }
        if (currentState.orderType.isBlank()) {
            onFailure("Please select order type"); return
        }
        if (currentState.paymentMethod.isBlank()) {
            onFailure("Please select payment method"); return
        }*/
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

                // Handle wallet payment (AuthRepository returns Result<Double>)
                if (currentState.paymentMethod.equals("Wallet", ignoreCase = true)) {
                    val walletResult = authRepository.getWalletBalance() // Result<Double>

                    if (walletResult.isFailure) {
                        val err = walletResult.exceptionOrNull()?.message ?: "Failed to get wallet balance"
                        _uiState.value = currentState.copy(isPlacingOrder = false, errorMessage = err)
                        onFailure(err)
                        return@launch
                    }

                    val balance: Double = walletResult.getOrNull() ?: 0.0

                    if (balance < totalAmount) {
                        val err = "Not enough wallet balance"
                        _uiState.value = currentState.copy(isPlacingOrder = false, errorMessage = err)
                        onFailure(err)
                        return@launch
                    }

                    // Deduct balance (updateWalletBalance returns Result<Unit>)
                    val updateResult = authRepository.updateWalletBalance(balance - totalAmount)
                    if (updateResult.isFailure) {
                        val err = updateResult.exceptionOrNull()?.message ?: "Failed to update wallet balance"
                        _uiState.value = currentState.copy(isPlacingOrder = false, errorMessage = err)
                        onFailure(err)
                        return@launch
                    }
                    // if success -> continue to place order
                }

                // Place order (assuming this returns Result<String>)
                val orderResult = ordersRepository.placeOrder(
                    customerName = currentState.customerName,
                    customerPhone = currentState.customerPhone,
                    cartItems = cartItems,
                    orderType = currentState.orderType,
                    paymentMethod = currentState.paymentMethod,
                    deliveryAddress = currentState.deliveryAddress.takeIf { it.isNotBlank() },
                    tableNumber = currentState.tableNumber.takeIf { it.isNotBlank() },
                    specialInstructions = currentState.specialInstructions
                )

                if (orderResult.isSuccess) {
                    val orderId = orderResult.getOrNull().orEmpty()
                    _uiState.value = currentState.copy(isPlacingOrder = false, orderPlaced = true)
                    clearCart()
                    onSuccess(orderId)
                } else {
                    val err = orderResult.exceptionOrNull()?.message ?: "Failed to place order"
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
