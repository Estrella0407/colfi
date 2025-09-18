// StaffOrdersUiState.kt
package com.example.colfi.ui.state

import com.example.colfi.data.model.OrderHistory

data class StaffOrdersUiState(
    val orders: List<OrderHistory> = emptyList(),
    val filteredOrders: List<OrderHistory> = emptyList(),
    val selectedTab: String = "all",
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isRefreshing: Boolean = false,
    val orderCounts: OrderCounts = OrderCounts()
) {
    val hasOrders: Boolean = orders.isNotEmpty()
    val hasFilteredOrders: Boolean = filteredOrders.isNotEmpty()
}

data class OrderCounts(
    val all: Int = 0,
    val dineIn: Int = 0,
    val pickUp: Int = 0,
    val delivery: Int = 0
)