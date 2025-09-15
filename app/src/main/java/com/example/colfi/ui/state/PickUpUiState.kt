// PickUpUiState
package com.example.colfi.ui.state

data class PickUpUiState(
    val storeName: String = "Colfi",
    val storeAddress: String = "G-07, Wisma New Asia, Jalan Raja Chulan, Bukit Ceylon, 50200 Kuala Lumpur, Wilayah (7.28 km away)",
    val selectedTime: String = "",
    val orderItemName: String = "Nutty Black (Hot)",
    val orderItemPrice: Double = 10.00,
    val paymentMethod: String = "Credit Card",
    val subtotal: Double = 10.00,
    val serviceTax: Double = 0.60,
    val netTotal: Double = 10.60
)
