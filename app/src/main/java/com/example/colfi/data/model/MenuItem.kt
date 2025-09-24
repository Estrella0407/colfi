// MenuItem
package com.example.colfi.data.model

import com.example.colfi.DrawableMapper

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val imageName: String = "",
    val availability: Boolean = true,
    val quantity: Int = 0,
    val minQuantity: Int = 0,
    val maxQuantity: Int = 100
) {
    val imageResId: Int
        get() = DrawableMapper.getDrawableForImageName(imageName)
}
