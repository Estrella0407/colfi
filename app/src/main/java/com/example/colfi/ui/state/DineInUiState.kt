// DineInUiState.kt
package com.example.colfi.ui.screens.dinein

import com.example.colfi.data.model.TableEntity

data class DineInUiState(
    val tables: List<TableEntity> = emptyList(),
    val selectedTable: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

