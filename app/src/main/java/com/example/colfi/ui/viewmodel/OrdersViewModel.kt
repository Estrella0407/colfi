// OrdersViewModel.kt
package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.data.repository.OrdersRepository
import com.example.colfi.ui.state.OrdersUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val ordersRepository: OrdersRepository = OrdersRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow("current")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        loadOrders()
    }

    fun selectTab(tab: String) {
        _selectedTab.value = tab
        loadOrders()
    }

    private fun loadOrders() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()

            when (_selectedTab.value) {
                "current" -> {
                    ordersRepository.getCurrentOrders(userId)
                        .onSuccess { orders ->
                            _uiState.value = _uiState.value.copy(
                                orders = orders,
                                isLoading = false
                            )
                        }
                        .onFailure { exception ->
                            _uiState.value = _uiState.value.copy(
                                orders = emptyList(),
                                isLoading = false,
                                errorMessage = exception.message ?: "Failed to load current orders"
                            )
                        }
                }
                "history" -> {
                    ordersRepository.getOrderHistory(userId)
                        .onSuccess { orders ->
                            _uiState.value = _uiState.value.copy(
                                orders = orders,
                                isLoading = false
                            )
                        }
                        .onFailure { exception ->
                            _uiState.value = _uiState.value.copy(
                                orders = emptyList(),
                                isLoading = false,
                                errorMessage = exception.message ?: "Failed to load order history"
                            )
                        }
                }
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            ordersRepository.cancelOrder(orderId)
                .onSuccess {
                    loadOrders() // Refresh the list
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Failed to cancel order"
                    )
                }
        }
    }
}