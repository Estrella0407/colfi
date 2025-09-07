// DineInViewModel.kt
package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.TableEntity
import com.example.colfi.data.repository.TableRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DineInViewModel(private val repository: TableRepository) : ViewModel() {

    private val _selectedTable = MutableStateFlow<String?>(null)
    val selectedTable: StateFlow<String?> = _selectedTable.asStateFlow()

    val tables: StateFlow<List<TableEntity>> =
        repository.allTables.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectTable(tableId: String) {
        _selectedTable.value = tableId
    }

    fun occupyTable(tableId: String) {
        viewModelScope.launch {
            repository.updateTableStatus(tableId, false)
        }
    }

    fun freeTable(tableId: String) {
        viewModelScope.launch {
            repository.updateTableStatus(tableId, true)
        }
    }
}
