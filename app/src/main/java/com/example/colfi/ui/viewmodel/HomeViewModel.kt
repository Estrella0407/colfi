// HomeViewModel.kt
package com.example.colfi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colfi.data.model.User
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.ui.state.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository = AuthRepository() // Using default constructor
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true)) // Start with loading true
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val quotes = listOf(
        "The perfect cup awaits",
        "Where coffee meets passion",
        "Brewing moments of joy",
        "Every sip tells a story",
        "Fall in Love, One Sip at a Time."
    )

    fun initialize(uid: String) { // Accepts UID
        Log.d("HomeViewModel", "Initializing for UID: $uid")
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            randomQuote = quotes.random(),
            user = null // Clear previous user to ensure fresh load/display
        )

        // Handle explicit "Guest" case based on a placeholder UID if your guest login returns one
        // Or if the UID passed for a guest is a specific known string.
        if (uid == "GUEST_USER_UID_PLACEHOLDER" || uid.startsWith("guest_anonymous_")) { // Example guest UID check
            Log.d("HomeViewModel", "Handling as Guest user for UID: $uid")
            // For a guest that has an anonymous UID and a Firestore doc:
            viewModelScope.launch {
                val guestUserObject = authRepository.getUserDataByUid(uid) // Try to fetch if guest data is in Firestore
                _uiState.value = _uiState.value.copy(
                    user = guestUserObject ?: User(displayName = "Guest", role="guest"), // Fallback guest
                    isLoading = false
                )
            }
            return
        }


        viewModelScope.launch {
            try {
                // Fetch the full User object from Firestore using the UID
                val userFromFirestore: User? = authRepository.getUserDataByUid(uid)

                if (userFromFirestore != null) {
                    Log.d("HomeViewModel", "Successfully fetched user data for UID $uid: ${userFromFirestore.displayName}")
                    _uiState.value = _uiState.value.copy(
                        user = userFromFirestore,
                        isLoading = false
                    )
                } else {
                    Log.w("HomeViewModel", "No user data found in Firestore for UID: $uid. User might be 'Guest' or new.")
                    // If it's not an explicit guest, but user data isn't found, decide on behavior.
                    // Perhaps the user from Firebase Auth is the source of truth for display name initially
                    // if Firestore doc creation failed or is pending.
                    // For now, setting user to null if not found.
                    _uiState.value = _uiState.value.copy(
                        user = null, // Or a default User object
                        isLoading = false,
                        errorMessage = "User profile not found in database."
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching user data for UID $uid from Firestore", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = null,
                    errorMessage = "Error loading user profile: ${e.message}"
                )
            }
        }
    }
}
