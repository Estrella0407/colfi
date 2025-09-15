package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
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
    fun updateTotals(price: Double) {
        val tax = price * 0.06
        val fee = 5.0
        val total = price + tax + fee
        _uiState.update {
            it.copy(
                subtotal = price,
                serviceTax = tax,
                netTotal = total,
                deliveryFee = fee
            )
        }
    }
}
