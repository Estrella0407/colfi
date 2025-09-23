// CartItem.kt
package com.example.colfi.data.model

import com.example.colfi.data.local.CartItemEntity

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int = 1
) {
    val totalPrice: Double
        get() = menuItem.price * quantity

}

fun CartItem.toEntity(): CartItemEntity {
    return CartItemEntity(
        menuItemId = menuItem.id,
        menuItemName = menuItem.name,
        menuItemDescription = menuItem.description,
        menuItemPrice = menuItem.price,
        menuItemCategory = menuItem.category,
        menuItemImageURL = menuItem.imageURL,
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
        quantity = quantity
    )
}
