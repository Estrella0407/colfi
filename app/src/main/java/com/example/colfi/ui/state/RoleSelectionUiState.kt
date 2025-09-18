// RoleSelectionUiState.kt
package com.example.colfi.ui.state

data class RoleSelectionUiState(
    val selectedRole: String = "",
    val isRoleSelected: Boolean = false,
    val lastSelectedRole: String = "",
    val errorMessage: String = ""
) {
    val isCustomerSelected: Boolean = selectedRole == "customer"
    val isStaffSelected: Boolean = selectedRole == "staff" || selectedRole == "barista"
}