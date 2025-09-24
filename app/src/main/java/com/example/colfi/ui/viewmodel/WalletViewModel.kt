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
        Log.d("WalletViewModelDebug", "initialize called. Current user: ${auth.currentUser?.uid}")

        _uiState.update {
            it.copy(isLoading = true, message = null)
        }

        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            Log.w("WalletViewModel", "No Firebase user authenticated")
            _uiState.update {
                it.copy(
                    balance = 0.0,
                    message = "Please log in to access wallet features",
                    isLoading = false
                )
            }
            return
        }

        val userId = firebaseUser.uid
        if (userId.isBlank()) {
            Log.w("WalletViewModel", "User ID is blank")
            _uiState.update {
                it.copy(
                    balance = 0.0,
                    message = "User identifier missing",
                    isLoading = false
                )
            }
            return
        }

        val userDoc = usersCollection.document(userId)
        userDoc.get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val balance = doc.getDouble("walletBalance") ?: 0.0
                    Log.d("WalletViewModel", "Wallet loaded for user '$userId'. Balance: $balance")
                    _uiState.update {
                        it.copy(balance = balance, isLoading = false)
                    }
                } else {
                    Log.w("WalletViewModel", "User document for '$userId' does not exist. Creating default wallet.")
                    // Create a default wallet document if it doesn't exist
                    userDoc.set(mapOf("walletBalance" to 0.0))
                        .addOnSuccessListener {
                            _uiState.update {
                                it.copy(balance = 0.0, isLoading = false)
                            }
                        }
                        .addOnFailureListener { e ->
                            _uiState.update {
                                it.copy(balance = 0.0, isLoading = false, message = "Error creating wallet")
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("WalletViewModel", "Error loading wallet: ${exception.message}")
                _uiState.update {
                    it.copy(
                        balance = 0.0,
                        isLoading = false,
                        message = "Error loading wallet: ${exception.message}"
                    )
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