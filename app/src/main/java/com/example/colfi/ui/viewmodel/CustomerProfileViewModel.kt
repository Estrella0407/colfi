package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.Customer
import com.example.colfi.data.model.User
import com.example.colfi.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomerProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _customer = MutableStateFlow<Customer?>(null)
    val customer: StateFlow<Customer?> = _customer.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    init {
        loadCustomer()
    }

    fun loadCustomer() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.getCurrentUser()

            result.fold(
                onSuccess = { user ->
                    // Check if the user is actually a customer
                    if (user is Customer) {
                        _customer.value = user
                    } else if (user.role == "customer") {
                        // If it's not a Customer instance but has customer role, try to cast/convert
                        _customer.value = Customer(
                            username = user.username,
                            displayName = user.displayName,
                            email = user.email,
                            role = user.role
                        )
                    } else {
                        _errorMessage.value = "Access denied: User is not a customer"
                        _customer.value = null
                    }
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load customer profile"
                    _customer.value = null
                }
            )
            _isLoading.value = false
        }
    }

    fun updateCustomerProfile(
        displayName: String,
        preferredPaymentMethod: String?,
        deliveryAddresses: List<String>
    ) {
        val currentCustomer = _customer.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val updatedCustomer = currentCustomer.copy(
                displayName = displayName,
                preferredPaymentMethod = preferredPaymentMethod,
                deliveryAddresses = deliveryAddresses
            )

            val result = authRepository.updateCustomerProfile(updatedCustomer)

            result.fold(
                onSuccess = {
                    _customer.value = updatedCustomer
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to update profile"
                }
            )
            _isLoading.value = false
        }
    }

    fun addDeliveryAddress(address: String) {
        val currentCustomer = _customer.value ?: return
        val updatedAddresses = currentCustomer.deliveryAddresses + address

        updateCustomerProfile(
            displayName = currentCustomer.displayName,
            preferredPaymentMethod = currentCustomer.preferredPaymentMethod,
            deliveryAddresses = updatedAddresses
        )
    }

    fun removeDeliveryAddress(address: String) {
        val currentCustomer = _customer.value ?: return
        val updatedAddresses = currentCustomer.deliveryAddresses - address

        updateCustomerProfile(
            displayName = currentCustomer.displayName,
            preferredPaymentMethod = currentCustomer.preferredPaymentMethod,
            deliveryAddresses = updatedAddresses
        )
    }

    fun updateWalletBalance(newBalance: Double) {
        val currentCustomer = _customer.value ?: return
        _customer.value = currentCustomer.copy(walletBalance = newBalance)
    }

    fun addPoints(points: Int) {
        val currentCustomer = _customer.value ?: return
        val newPoints = currentCustomer.points + points
        val newTier = calculateTier(newPoints)

        _customer.value = currentCustomer.copy(
            points = newPoints,
            tier = newTier
        )
    }

    private fun calculateTier(points: Int): Int {
        return when {
            points < 100 -> 0
            points < 500 -> 1
            points < 1000 -> 2
            points < 2000 -> 3
            else -> 4
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logoutUser()
                _customer.value = null
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
        loadCustomer()
    }

}