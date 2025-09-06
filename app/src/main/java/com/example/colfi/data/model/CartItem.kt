//Cart Item
package com.example.colfi.data.model

data class CartItem(
    val menuItem: MenuItem,
    val option: String = "Hot",
    val quantity: Int = 1
) {
    val totalPrice: Double
        get() = menuItem.price * quantity
}
