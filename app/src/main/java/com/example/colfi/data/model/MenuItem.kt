// MenuItem
package com.example.colfi.data.model

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageURL: String = "",
    val categoryID: String = "",
    val availability: Boolean = true
)