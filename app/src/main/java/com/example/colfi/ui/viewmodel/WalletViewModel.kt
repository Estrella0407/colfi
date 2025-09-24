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
    private val usersCollection = firestore.collection("users")

    fun initialize() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            _uiState.update {
                it.copy(
                    balance = 0.0,
                    isLoading = false,
                    message = "Please log in to access wallet features"
                )
            }
            return
        }

        val userId = firebaseUser.uid

        _uiState.update { it.copy(isLoading = true, message = null) }

        // Listen to wallet changes in realtime
        usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "Error loading wallet: ${error.message}"
                        )
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val balance = snapshot.getDouble("walletBalance") ?: 0.0
                    _uiState.update { it.copy(balance = balance, isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(balance = 0.0, isLoading = false, message = "No wallet found")
                    }
                }
            }
    }

    fun topUp(amount: Double) {
        _uiState.update { it.copy(isLoading = true, message = null) }

        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = "Top-up failed: Please log in first"
                )
            }
            return
        }

        val uid = firebaseUser.uid
        if (amount <= 0) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = "Top-up amount must be positive"
                )
            }
            return
        }

        val userDocRef = usersCollection.document(uid)

        viewModelScope.launch {
            try {
                val snapshot = userDocRef.get().await()
                val currentBalance = if (snapshot.exists()) {
                    snapshot.getDouble("walletBalance") ?: 0.0
                } else {
                    // Create wallet if it doesn't exist
                    userDocRef.set(mapOf("walletBalance" to 0.0)).await()
                    0.0
                }

                val newBalance = currentBalance + amount
                userDocRef.update("walletBalance", newBalance).await()

                _uiState.update {
                    it.copy(
                        balance = newBalance,
                        isLoading = false,
                        message = "Top-up RM${String.format("%.2f", amount)} successful!"
                    )
                }
                initialize()

            } catch (e: Exception) {
                Log.e("WalletViewModel", "Top-up error: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Top-up failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectMethod(method: String) {
        _uiState.update {
            it.copy(selectedMethod = method)
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}