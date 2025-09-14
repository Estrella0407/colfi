// CartItem.kt
package com.example.colfi.data.model

import com.example.colfi.data.local.CartItemEntity

data class CartItem(
    val menuItem: MenuItem,
    val selectedTemperature: String? = null,
    val selectedSugarLevel: String? = null,
    val quantity: Int = 1
) {
    val options: String
        get() = buildString {
            selectedTemperature?.let { append("$it ") }
            selectedSugarLevel?.let { append("$it ") }
        }.trim()

    val totalPrice: Double
        get() = menuItem.price * quantity

    companion object {
        val TEMPERATURE_OPTIONS = listOf("Hot", "Cold")
        val SUGAR_LEVEL_OPTIONS = listOf("No Sugar", "Less Sugar", "Normal Sugar", "Extra Sugar")
    }
}

fun CartItem.toEntity(): CartItemEntity {
    return CartItemEntity(
        menuItemId = menuItem.id,
        menuItemName = menuItem.name,
        menuItemDescription = menuItem.description,
        menuItemPrice = menuItem.price,
        menuItemCategory = menuItem.category,
        menuItemImageURL = menuItem.imageURL,
        selectedTemperature = selectedTemperature,
        selectedSugarLevel = selectedSugarLevel,
        quantity = quantity
    )
}

fun CartItemEntity.toCartItem(): CartItem {
    return CartItem(
        menuItem = MenuItem(
            id = menuItemId,
            name = menuItemName,
            description = menuItemDescription,
            price = menuItemPrice,
            category = menuItemCategory,
            imageURL = menuItemImageURL,
            availability = true
        ),
        selectedTemperature = selectedTemperature,
        selectedSugarLevel = selectedSugarLevel,
        quantity = quantity
    )
}
