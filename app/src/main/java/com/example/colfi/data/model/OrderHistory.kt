package com.example.colfi.data.model

data class OrderHistory(
    val orderId: String,
    val orderDate: String,
    val orderStatus: String,
    val orderTotal: Double,
    val orderItems: List<OrderItem>

)