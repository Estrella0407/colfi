package com.example.colfi.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.colfi.ui.state.WalletUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class WalletViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState(balance = 50.0)) // default RM50
    val uiState: StateFlow<WalletUiState> = _uiState

    fun selectMethod(method: String) {
        _uiState.update { it.copy(selectedMethod = method) }
    }

    fun topUp(amount: Double) {
        _uiState.update {
            it.copy(
                balance = it.balance + amount,
                message = "Top-up RM${String.format("%.2f", amount)} successful!"
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
