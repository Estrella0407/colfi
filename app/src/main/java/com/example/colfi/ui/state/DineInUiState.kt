// DineInUiState.kt
package com.example.colfi.ui.state

import com.example.colfi.data.model.TableEntity

data class DineInUiState(
    val tables: List<TableEntity> = emptyList(),
    val selectedTable: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

