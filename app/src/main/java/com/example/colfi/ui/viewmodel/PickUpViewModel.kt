// PickUpViewModel.kt
package com.example.colfi.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.colfi.data.model.CartItem
import com.example.colfi.ui.state.PickUpUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class PickUpViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PickUpUiState())
    val uiState: StateFlow<PickUpUiState> = _uiState

    fun setStoreInfo(name: String, address: String) {
        _uiState.update { it.copy(storeName = name, storeAddress = address) }
    }
    // User selects pick up time
    fun selectTime(time: String) {
        _uiState.update { it.copy(selectedTime = time) }
    }

    // User changes payment method
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateFuturePickUpTimes(): List<String> {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        val now = LocalTime.now()

        val opening = LocalTime.of(7, 0)
        val closing = LocalTime.of(16, 0)

        // If now is before opening → start at opening
        // If now is after opening → start at now + 30 mins
        var start = if (now.isBefore(opening)) opening else now.plusMinutes(30)

        // Round start to next half-hour
        start = when {
            start.minute in 1..29 -> start.withMinute(30).withSecond(0).withNano(0)
            start.minute in 31..59 -> start.plusHours(1).withMinute(0).withSecond(0).withNano(0)
            else -> start.withSecond(0).withNano(0) // already exact
        }

        val slots = mutableListOf<String>()
        var slot = start

        while (!slot.isAfter(closing)) {
            slots.add(slot.format(formatter))
            slot = slot.plusMinutes(30)
        }

        return slots
    }
}
