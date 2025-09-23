// CartItemEntity.kt
package com.example.colfi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val menuItemId: String,
    val menuItemName: String,
    val menuItemDescription: String,
    val menuItemPrice: Double,
    val menuItemCategory: String,
    val menuItemImageURL: String,
    val quantity: Int = 1,
    val dateAdded: Long = System.currentTimeMillis()
) {
    val totalPrice: Double
        get() = menuItemPrice * quantity
}