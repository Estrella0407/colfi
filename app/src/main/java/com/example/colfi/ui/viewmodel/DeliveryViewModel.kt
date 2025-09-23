package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.colfi.data.model.CartItem
import com.example.colfi.ui.state.DeliveryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class DeliveryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DeliveryUiState())
    val uiState: StateFlow<DeliveryUiState> = _uiState

    // Set or update customer delivery address
    fun setCustomerAddress(address: String) {
        _uiState.update { it.copy(customerAddress = address) }
    }

    // Add or update delivery instructions
    fun setDeliveryInstruction(instruction: String) {
        _uiState.update { it.copy(deliveryInstruction = instruction) }
    }

    // Change payment method
    fun selectPaymentMethod(method: String) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    // Recalculate totals if needed
    fun updateTotals(cartItems: List<CartItem>) {
        val subtotal: Double = cartItems.sumOf { cartItem ->
            cartItem.totalPrice
        }
        val tax = subtotal * 0.06
        val total = subtotal + tax
        _uiState.update {
            it.copy(
                subtotal = subtotal,
                serviceTax = tax,
                netTotal = total
            )
        }
    }
}
