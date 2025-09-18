// CheckoutUiState.kt
package com.example.colfi.ui.state

data class CheckoutUiState(
    val customerName: String = "",
    val customerPhone: String = "",
    val orderType: String = "", // "dine_in", "pick_up", "delivery"
    val paymentMethod: String = "", // "cash", "card", "ewallet"
    val deliveryAddress: String = "",
    val tableNumber: String = "",
    val specialInstructions: String = "",
    val isPlacingOrder: Boolean = false,
    val orderPlaced: Boolean = false,
    val errorMessage: String = ""
) {
    val isDelivery: Boolean = orderType == "delivery"
    val isDineIn: Boolean = orderType == "dine_in"
    val isPickUp: Boolean = orderType == "pick_up"

    val canPlaceOrder: Boolean =
        customerName.isNotBlank() &&
                customerPhone.isNotBlank() &&
                orderType.isNotBlank() &&
                paymentMethod.isNotBlank() &&
                (!isDelivery || deliveryAddress.isNotBlank())
}