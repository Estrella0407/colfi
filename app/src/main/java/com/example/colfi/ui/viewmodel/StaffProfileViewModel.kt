package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.Staff
import com.example.colfi.data.model.User
import com.example.colfi.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StaffProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _staff = MutableStateFlow<Staff?>(null)
    val staff: StateFlow<Staff?> = _staff.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    init {
        loadStaff()
    }

    fun loadStaff() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.getCurrentUser()

            result.fold(
                onSuccess = { user ->
                    // Check if the user is actually a staff member
                    if (user is Staff) {
                        _staff.value = user
                    } else if (user.role == "staff") {
                        // If it's not a Staff instance but has staff role, try to cast/convert
                        _staff.value = Staff(
                            username = user.username,
                            displayName = user.displayName,
                            email = user.email,
                            role = user.role
                        )
                    } else {
                        _errorMessage.value = "Access denied: User is not a staff member"
                        _staff.value = null
                    }
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load staff profile"
                    _staff.value = null
                }
            )
            _isLoading.value = false
        }
    }

    fun updateStaffProfile(
        displayName: String,
        position: String,
        specialty: String?,
    ) {
        val currentStaff = _staff.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val userId = authRepository.getCurrentUserId()
                ?: throw Exception("User not authenticated")


            val updatedStaff = currentStaff.copy(
                displayName = displayName,
                position = position,
                specialty = specialty,
                staffId = userId
            )

            val result = authRepository.updateStaffProfile(updatedStaff)

            result.fold(
                onSuccess = {
                    _staff.value = updatedStaff
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to update profile"
                }
            )
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logoutUser()
                _staff.value = null
                _isLoggedOut.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Failed to logout: ${e.message}"
            }
        }
    }

    fun onLogoutHandled() {
        _isLoggedOut.value = false
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun refreshProfile() {
        loadStaff()
    }
}