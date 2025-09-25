// OrderHistory.kt
package com.example.colfi.data.model

import com.google.firebase.firestore.PropertyName
import java.util.*

data class OrderHistory(
    val orderId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val orderItems: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val orderType: String = "", // "dine_in", "pick_up", "delivery"
    val orderStatus: String = "", // "pending", "preparing", "ready", "delivering", "completed", "cancelled"
    val orderDate: Long = System.currentTimeMillis(),
    val estimatedTime: Int = 0, // in minutes
    val specialInstructions: String = "",
    val paymentMethod: String = "",
    val deliveryAddress: String? = null,
    val tableNumber: String? = null
) {
    val formattedOrderDate: String
        get() = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            .format(Date(orderDate))

    val formattedTotalAmount: String
        get() = String.format("RM %.2f", totalAmount)

    val canBeCancelled: Boolean
        get() = orderStatus in listOf("pending", "preparing")

    val statusColor: androidx.compose.ui.graphics.Color
        get() = when (orderStatus) {
            "pending" -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
            "preparing" -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
            "ready" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
            "delivering" -> androidx.compose.ui.graphics.Color(0xFF9C27B0) // Purple
            "completed" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
            "cancelled" -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
            else -> androidx.compose.ui.graphics.Color.Gray
        }
}