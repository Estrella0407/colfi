package com.example.colfi.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.TableEntity
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.data.repository.OrdersRepository
import com.example.colfi.data.repository.TableRepository
import com.example.colfi.ui.state.DineInUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DineInViewModel(private val repository: TableRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DineInUiState())
    val uiState: StateFlow<DineInUiState> = _uiState.asStateFlow()
    private val ordersRepository = OrdersRepository()
    private val authRepository = AuthRepository()


    init {
        viewModelScope.launch {
    // Pre-populate if empty
            val existing = repository.getAllTablesOnce()
            if (existing.isEmpty()) {
                val tables = (1..9).map { index ->
                    TableEntity(
                        tableId = "T$index",
                        isAvailable = true
                    )
                }
                repository.insertAllTables(tables)
            }


    // Observe changes
            repository.allTables.collect { tables ->
                _uiState.value = DineInUiState(tables = tables)
            }
        }
    }


    fun updateTableStatus(tableId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.updateTableStatus(tableId, isAvailable)
        }
    }

    fun createDineInOrder(tableId: String) {
        viewModelScope.launch {
            // First update table status
            updateTableStatus(tableId, false)

            // Then create the order using the repository
            val result = ordersRepository.createDineInBooking(
                customerId = authRepository.getCurrentUserId(),
                customerName = authRepository.getCurrentUserName(),
                customerPhone = authRepository.getCurrentUserPhone(),
                tableNumber = tableId,
                paymentMethod = "",
                specialInstructions = "Table booking"
            )

            result.onSuccess { orderId ->
                _uiState.value = _uiState.value.copy(
                    successMessage = "Table booked successfully! Order #$orderId"
                )
            }.onFailure { exception ->
                // Revert table status if order creation fails
                updateTableStatus(tableId, true)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Booking failed: ${exception.message}"
                )
            }
        }
    }
}