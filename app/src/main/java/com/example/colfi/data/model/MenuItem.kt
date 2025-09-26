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
) {
    val imageResId: Int
        get() = DrawableMapper.getDrawableForImageName(imageName)
}
