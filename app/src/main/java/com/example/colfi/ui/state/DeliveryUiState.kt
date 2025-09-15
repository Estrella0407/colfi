package com.example.colfi.ui.state

data class DeliveryUiState(
    val storeName: String = "Colfi",
    val customerAddress: String = "", // Null if no address yet
    val deliveryInstruction: String = "",
    val orderItemName: String = "Nutty Black (Iced)",
    val orderItemPrice: Double = 12.00,
    val paymentMethod: String = "Credit Card",
    val subtotal: Double = 12.00,
    val serviceTax: Double = 0.72,
    val netTotal: Double = 12.72,
    val deliveryFee : Double = 5.0
)
