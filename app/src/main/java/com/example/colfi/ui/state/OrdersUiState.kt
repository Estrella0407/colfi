package com.example.colfi.ui.state

import com.example.colfi.data.model.OrderHistory

data class OrdersUiState (
    val orders: List<OrderHistory> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)