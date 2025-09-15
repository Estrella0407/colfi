//Pick Up Item
package com.example.colfi.data.model

data class PickUpItem(
    val id: String,              // Unique ID for the item
    val name: String,            // Item name (e.g., "Latte")
    val quantity: Int = 1,       // Quantity chosen
    val price: Double,           // Price per item
    val imageUrl: String = ""    // Optional image for the item
) {
    val totalPrice: Double
        get() = price * quantity
}
