package com.example.colfi.data.model

data class OrderItem(
    val id: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val temperature: String = "", // Hot/Cold
    val customizations: List<String> = emptyList()
)