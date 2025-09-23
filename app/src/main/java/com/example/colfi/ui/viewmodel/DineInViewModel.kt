package com.example.colfi.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.TableEntity
import com.example.colfi.data.repository.TableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class DineInUiState(
    val tables: List<TableEntity> = emptyList()
)


class DineInViewModel(private val repository: TableRepository) : ViewModel() {


    private val _uiState = MutableStateFlow(DineInUiState())
    val uiState: StateFlow<DineInUiState> = _uiState.asStateFlow()


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
}