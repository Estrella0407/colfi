// WalletViewModel.kt
package com.example.colfi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.ui.state.WalletUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WalletViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val walletsCollection = firestore.collection("wallets")

    fun initialize(userId: String) {
        Log.d("WalletViewModelDebug", "initialize called for: $userId. Current isLoading: ${_uiState.value.isLoading}")
        _uiState.update {
            it.copy(isLoading = true, message = null)
        }

        if (userId.isBlank() || userId == "Guest" || userId.startsWith("guest_anonymous_") || userId == "GUEST_USER_UID_PLACEHOLDER") {
            Log.w("WalletViewModel", "Initialization skipped for blank or Guest user: $userId")
            val guestMessage = if (userId.isBlank()) "User identifier missing." else "Guest wallet not available"

            _uiState.update {
                it.copy(balance = 0.0, message = guestMessage, isLoading = false)
            }
            return
        }

        val walletDoc = walletsCollection.document(userId)
        walletDoc.get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val balance = doc.getDouble("balance") ?: 0.0
                    Log.d("WalletViewModel", "Read initial balance for '$userId'. Balance: $balance")
                    _uiState.update {
                        it.copy(balance = balance, isLoading = false)
                    }
                } else {
                    Log.w("WalletViewModel", "Wallet document for '$userId' does not exist. Setting balance to 0.")
                    _uiState.update {
                        it.copy(balance = 0.0, isLoading = false, message = "Wallet not found for '$userId'.")
                    }
                }
            }
            .addOnFailureListener { exception ->
                _uiState.update { it.copy(balance = 0.0, isLoading = false, message = "Error loading wallet.") }
            }
    }

    fun selectMethod(method: String) {
        _uiState.update {
            it.copy(selectedMethod = method)
        }
    }

    fun topUp(amount: Double) {
        _uiState.update { it.copy(isLoading = true, message = null) }

        val firebaseCurrentUser = auth.currentUser
        if (firebaseCurrentUser == null) {
            _uiState.update { it.copy(isLoading = false, message = "Top-up failed: You must be logged in.") }
            return
        }

        val uid = firebaseCurrentUser.uid
        if (amount <= 0) {
            _uiState.update { it.copy(isLoading = false, message = "Top-up amount must be positive.") }
            return
        }

        val walletDocRef = walletsCollection.document(uid)

        viewModelScope.launch {
            try {
                val snapshot = walletDocRef.get().await()
                val currentBalance = if (snapshot.exists()) {
                    snapshot.getDouble("balance") ?: 0.0
                } else {
                    Log.w("WalletViewModel", "Wallet document not found for $uid. Cannot top-up.")
                    _uiState.update {
                        it.copy(isLoading = false, message = "Wallet not set up. Please contact support.")
                    }
                    return@launch
                }

                val newBalance = currentBalance + amount
                walletDocRef.update("balance", newBalance).await()

                _uiState.update {
                    it.copy(
                        balance = newBalance,
                        isLoading = false,
                        message = "Top-up RM${String.format("%.2f", amount)} successful!"
                    )
                }

            } catch (e: FirebaseFirestoreException) {
                _uiState.update { it.copy(isLoading = false, message = "Top-up failed! Firestore Error: ${e.code} - ${e.message}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, message = "An unexpected error occurred during top-up.") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
