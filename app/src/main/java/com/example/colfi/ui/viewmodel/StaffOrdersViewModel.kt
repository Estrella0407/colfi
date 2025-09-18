// StaffOrdersViewModel.kt
package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.repository.OrdersRepository
import com.example.colfi.ui.state.StaffOrdersUiState
import com.example.colfi.ui.state.OrderCounts
import com.example.colfi.data.model.OrderHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StaffOrdersViewModel(
    private val ordersRepository: OrdersRepository = OrdersRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(StaffOrdersUiState())
    val uiState: StateFlow<StaffOrdersUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow("all")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    init {
        initialize()
        startRealtimeUpdates()
    }

    private fun initialize() {
        loadOrders()
    }

    private fun startRealtimeUpdates() {
        ordersRepository.observeCurrentOrders { orders ->
            _uiState.value = _uiState.value.copy(
                orders = orders,
                isLoading = false
            )
            updateFilteredOrders(orders)
            updateOrderCounts(orders)
        }
    }

    fun selectTab(tab: String) {
        _selectedTab.value = tab
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        updateFilteredOrders(_uiState.value.orders)
    }

    private fun loadOrders() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

        viewModelScope.launch {
            val result = when (_selectedTab.value) {
                "all" -> ordersRepository.getCurrentOrders()
                "dine_in" -> ordersRepository.getOrdersByType("dine_in")
                "pick_up" -> ordersRepository.getOrdersByType("pick_up")
                "delivery" -> ordersRepository.getOrdersByType("delivery")
                else -> ordersRepository.getCurrentOrders()
            }

            result.onSuccess { orders ->
                _uiState.value = _uiState.value.copy(
                    orders = orders,
                    isLoading = false,
                    errorMessage = ""
                )
                updateFilteredOrders(orders)
                updateOrderCounts(orders)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    orders = emptyList(),
                    filteredOrders = emptyList(),
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to load orders"
                )
            }
        }
    }

    private fun updateFilteredOrders(allOrders: List<OrderHistory>) {
        val filtered = when (_selectedTab.value) {
            "all" -> allOrders
            "dine_in" -> allOrders.filter { it.orderType == "dine_in" }
            "pick_up" -> allOrders.filter { it.orderType == "pick_up" }
            "delivery" -> allOrders.filter { it.orderType == "delivery" }
            else -> allOrders
        }

        _uiState.value = _uiState.value.copy(filteredOrders = filtered)
    }

    private fun updateOrderCounts(orders: List<OrderHistory>) {
        val counts = OrderCounts(
            all = orders.size,
            dineIn = orders.count { it.orderType == "dine_in" },
            pickUp = orders.count { it.orderType == "pick_up" },
            delivery = orders.count { it.orderType == "delivery" }
        )

        _uiState.value = _uiState.value.copy(orderCounts = counts)
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            ordersRepository.updateOrderStatus(orderId, newStatus)
                .onSuccess {
                    // Orders will be updated via real-time listener
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Failed to update order status"
                    )
                }
        }
    }

    fun refreshOrders() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadOrders()
        _uiState.value = _uiState.value.copy(isRefreshing = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any listeners if needed
    }
}