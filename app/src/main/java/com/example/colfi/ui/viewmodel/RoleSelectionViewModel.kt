// RoleSelectionViewModel.kt
package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.ui.state.RoleSelectionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RoleSelectionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RoleSelectionUiState())
    val uiState: StateFlow<RoleSelectionUiState> = _uiState.asStateFlow()

    fun selectRole(role: String) {
        _uiState.value = _uiState.value.copy(
            selectedRole = role,
            isRoleSelected = true
        )

        // Optional: Save role preference to local storage or user preferences
        saveRolePreference(role)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            selectedRole = "",
            isRoleSelected = false
        )
    }

    private fun saveRolePreference(role: String) {
        viewModelScope.launch {
            try {
                // You can implement SharedPreferences or DataStore here
                // For now, we'll just keep it in memory
                _uiState.value = _uiState.value.copy(lastSelectedRole = role)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to save role preference: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}