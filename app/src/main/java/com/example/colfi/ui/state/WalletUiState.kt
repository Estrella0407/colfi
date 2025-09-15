package com.example.colfi.ui.state

data class WalletUiState(
    val balance: Double = 0.0,
    val selectedMethod: String = "TnG",
    val message: String? = null
)
