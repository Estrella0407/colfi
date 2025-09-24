// AuthRepository.kt
package com.example.colfi.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.colfi.data.model.Customer
import com.example.colfi.data.model.Guest
import com.example.colfi.data.model.Staff
import com.example.colfi.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context? = null) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // Use SharedPreferences to persist guest state
    private val prefs: SharedPreferences? = context?.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val GUEST_MODE_KEY = "is_guest_mode"
    private val GUEST_USERNAME_KEY = "guest_username"
    private val GUEST_DISPLAY_NAME_KEY = "guest_display_name"
    private val GUEST_EMAIL_KEY = "guest_email"
    private val GUEST_ROLE_KEY = "guest_role"
    private val IS_FIRST_TIME_GUEST_KEY = "is_first_time_guest"

    // Store guest state in memory for performance
    private var currentGuestUser: Guest? = null
    private var isGuestMode: Boolean = false

    init {
        // Restore guest state from SharedPreferences on initialization
        restoreGuestState()
    }

    private fun restoreGuestState() {
        prefs?.let { preferences ->
            isGuestMode = preferences.getBoolean(GUEST_MODE_KEY, false)
            if (isGuestMode) {
                val username = preferences.getString(GUEST_USERNAME_KEY, "guest") ?: "guest"
                val displayName = preferences.getString(GUEST_DISPLAY_NAME_KEY, "Guest") ?: "Guest"
                val email = preferences.getString(GUEST_EMAIL_KEY, "guest@local") ?: "guest@local"
                val role = preferences.getString(GUEST_ROLE_KEY, "guest") ?: "guest"

                currentGuestUser = Guest(
                    username = username,
                    displayName = displayName,
                    email = email,
                    role = role
                )
                Log.d("AuthRepo", "Restored guest state from SharedPreferences")
            }
        }
    }

    private fun saveGuestState(guest: Guest) {
        prefs?.edit()?.apply {
            putBoolean(GUEST_MODE_KEY, true)
            putString(GUEST_USERNAME_KEY, guest.username)
            putString(GUEST_DISPLAY_NAME_KEY, guest.displayName)
            putString(GUEST_EMAIL_KEY, guest.email)
            putString(GUEST_ROLE_KEY, guest.role)
            apply()
        }
        Log.d("AuthRepo", "Saved guest state to SharedPreferences")
    }

    private fun clearGuestState() {
        prefs?.edit()?.apply {
            putBoolean(GUEST_MODE_KEY, false)
            remove(GUEST_USERNAME_KEY)
            remove(GUEST_DISPLAY_NAME_KEY)
            remove(GUEST_EMAIL_KEY)
            remove(GUEST_ROLE_KEY)
            apply()
        }
        Log.d("AuthRepo", "Cleared guest state from SharedPreferences")
    }

    suspend fun login(usernameOrEmail: String, password: String): Result<User> {
        return try {
            // Clear guest mode when logging in properly
            clearGuestState()
            isGuestMode = false
            currentGuestUser = null

            // Reset first-time guest flag since user is logging in properly
            prefs?.edit()?.apply {
                putBoolean(IS_FIRST_TIME_GUEST_KEY, true)
                apply()
            }

            val isEmail = usernameOrEmail.contains("@")

            val email = if (isEmail) {
                usernameOrEmail
            } else {
                // Look up email by username
                val userQuery = usersCollection
                    .whereEqualTo("username", usernameOrEmail)
                    .limit(1)
                    .get()
                    .await()

                if (userQuery.isEmpty) {
                    throw Exception("Username '$usernameOrEmail' not found")
                }

                val userDoc = userQuery.documents.first()
                userDoc.getString("email") ?: throw Exception("Email not found for username")
            }

            // Authenticate with Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser == null) {
                throw Exception("Authentication failed")
            }

            // Get user data from Firestore after successful authentication
            getCurrentUser()

        } catch (e: Exception) {
            Log.e("AuthRepo", "Login failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        // Check guest mode first
        if (isGuestMode && currentGuestUser != null) {
            Log.d("AuthRepo", "Returning guest user: ${currentGuestUser!!.displayName}")
            return Result.success(currentGuestUser!!)
        }

        val firebaseUser = auth.currentUser
        Log.d("AuthRepo", "Getting current user - Firebase user: ${firebaseUser != null}, UID: ${firebaseUser?.uid}")

        return if (firebaseUser != null) {
            try {
                Log.d("AuthRepo", "Fetching user document from Firestore for UID: ${firebaseUser.uid}")
                val documentSnapshot = usersCollection.document(firebaseUser.uid).get().await()

                if (documentSnapshot.exists()) {
                    val role = documentSnapshot.getString("role") ?: "customer"
                    Log.d("AuthRepo", "Found user document with role: $role")

                    val user = when (role) {
                        "staff" -> documentSnapshot.toObject(Staff::class.java)
                        "customer" -> documentSnapshot.toObject(Customer::class.java)
                        else -> documentSnapshot.toObject(Customer::class.java)
                    }

                    if (user != null) {
                        Log.d("AuthRepo", "Successfully parsed user: ${user.displayName}, Role: ${user.role}")
                        Result.success(user)
                    } else {
                        Log.e("AuthRepo", "Failed to parse user data - user object is null")
                        Result.failure(Exception("Failed to parse user data"))
                    }
                } else {
                    Log.e("AuthRepo", "User document not found in Firestore for UID: ${firebaseUser.uid}")
                    Result.failure(Exception("User document not found"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepo", "Error getting current user: ${e.message}", e)
                Result.failure(e)
            }
        } else {
            Log.d("AuthRepo", "No Firebase user logged in")
            Result.failure(Exception("No user logged in"))
        }
    }

    suspend fun registerUser(
        username: String,
        email: String,
        password: String,
        displayName: String,
        role: String
    ): Result<String> {
        return when (role) {
            "customer" -> registerCustomer(username, email, password, displayName)
            "staff" -> registerStaff(username, email, password, displayName, "General Staff")
            else -> Result.failure(Exception("Invalid role specified"))
        }
    }

    suspend fun registerCustomer(
        username: String,
        email: String,
        password: String,
        displayName: String,
        balance: Double = 0.0,
        points: Int = 0
    ): Result<String> {
        return try {
            val existingUser = usersCollection
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (!existingUser.isEmpty) {
                return Result.failure(Exception("Username already exists"))
            }

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            val customerData = hashMapOf(
                "username" to username,
                "displayName" to displayName,
                "email" to email,
                "role" to "customer",
                "walletBalance" to balance,
                "points" to points,
                "vouchers" to 0,
                "tier" to 0,
                "preferredPaymentMethod" to null,
                "deliveryAddresses" to emptyList<String>(),
                "orderHistory" to emptyList<String>()
            )

            usersCollection.document(firebaseUser.uid).set(customerData).await()
            Result.success("Customer registration successful")

        } catch (e: Exception) {
            Log.e("AuthRepo", "Customer registration failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun registerStaff(
        username: String,
        email: String,
        password: String,
        displayName: String,
        position: String,
        specialty: String? = null,
        staffId: String? = null
    ): Result<String> {
        return try {
            val existingUser = usersCollection
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (!existingUser.isEmpty) {
                return Result.failure(Exception("Username already exists"))
            }

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            val staffData = hashMapOf(
                "username" to username,
                "displayName" to displayName,
                "email" to email,
                "role" to "staff",
                "position" to position,
                "specialty" to specialty,
                "staffId" to staffId,
                "staffSince" to System.currentTimeMillis().toString(),
            )

            usersCollection.document(firebaseUser.uid).set(staffData).await()
            Result.success("Staff registration successful")

        } catch (e: Exception) {
            Log.e("AuthRepo", "Staff registration failed: ${e.message}")
            Result.failure(e)
        }
    }

    // Simpler guest login that doesn't interfere with Firebase Auth
    suspend fun loginAsGuest(): Result<Guest> {
        return try {
            // Don't sign out Firebase user - just set guest mode
            val guestUser = Guest(
                username = "guest",
                displayName = "Guest",
                email = "guest@local",
                role = "guest"
            )

            isGuestMode = true
            currentGuestUser = guestUser
            saveGuestState(guestUser)
            markGuestNotFirstTime() // Mark that guest has been used

            Log.d("AuthRepo", "Guest login successful")
            Result.success(guestUser)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Guest login failed: ${e.message}")
            Result.failure(e)
        }
    }

    // Check if this is a first-time guest (should go to login screen)
    fun isFirstTimeGuest(): Boolean {
        return prefs?.getBoolean(IS_FIRST_TIME_GUEST_KEY, true) ?: true
    }

    // Mark that guest has been used before (not first time anymore)
    private fun markGuestNotFirstTime() {
        prefs?.edit()?.apply {
            putBoolean(IS_FIRST_TIME_GUEST_KEY, false)
            apply()
        }
    }

    // Check current authentication state
    fun isUserLoggedIn(): Boolean {
        val hasFirebaseUser = auth.currentUser != null
        val isFirstTime = isFirstTimeGuest()
        Log.d("AuthRepo", "Firebase user: $hasFirebaseUser, Guest mode: $isGuestMode, First time: $isFirstTime, UID: ${auth.currentUser?.uid}")

        // If it's a first-time guest, they should go to login screen
        if (isGuestMode && isFirstTime) {
            Log.d("AuthRepo", "First-time guest detected, should go to login screen")
            return false
        }

        return hasFirebaseUser || isGuestMode
    }

    fun isGuestUser(): Boolean {
        return isGuestMode
    }

    fun logoutUser() {
        auth.signOut()
        clearGuestState()
        isGuestMode = false
        currentGuestUser = null

        // Reset first-time guest flag so next time they'll go to login
        prefs?.edit()?.apply {
            putBoolean(IS_FIRST_TIME_GUEST_KEY, true)
            apply()
        }
        Log.d("AuthRepo", "Logged out user and reset first-time guest flag")
    }

    // Update profile methods remain the same
    suspend fun updateCustomerProfile(customer: Customer): Result<Unit> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null && !isGuestMode) {
            try {
                val customerData = hashMapOf(
                    "username" to customer.username,
                    "displayName" to customer.displayName,
                    "email" to customer.email,
                    "role" to "customer",
                    "walletBalance" to customer.walletBalance,
                    "points" to customer.points,
                    "vouchers" to customer.vouchers,
                    "tier" to customer.tier,
                    "preferredPaymentMethod" to customer.preferredPaymentMethod,
                    "deliveryAddresses" to customer.deliveryAddresses
                )

                usersCollection.document(firebaseUser.uid).update(customerData).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("No authenticated user"))
        }
    }

    suspend fun updateStaffProfile(staff: Staff): Result<Unit> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null && !isGuestMode) {
            try {
                val staffData = mapOf<String, Any>(
                    "username" to staff.username,
                    "displayName" to staff.displayName,
                    "email" to staff.email,
                    "role" to "staff",
                    "position" to staff.position,
                    "specialty" to (staff.specialty ?: ""),
                    "staffId" to (staff.staffId ?: ""),
                    "staffSince" to staff.staffSince,
                )

                usersCollection.document(firebaseUser.uid).update(staffData).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("No authenticated user"))
        }
    }

    // Query methods remain the same
    suspend fun getAllStaff(): Result<List<Staff>> {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("role", "staff")
                .get()
                .await()

            val staffList = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(Staff::class.java)
            }

            Result.success(staffList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllCustomers(): Result<List<Customer>> {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("role", "customer")
                .get()
                .await()

            val customerList = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(Customer::class.java)
            }

            Result.success(customerList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCustomersByTier(tier: Int): Result<List<Customer>> {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("role", "customer")
                .whereEqualTo("tier", tier)
                .get()
                .await()

            val customerList = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(Customer::class.java)
            }

            Result.success(customerList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWalletBalance(): Result<Double> {
        return try {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Log.w("AuthRepo", "getWalletBalance: No user logged in")
                return Result.failure(Exception("No user logged in to fetch wallet balance."))
            }
            val doc = usersCollection.document(uid).get().await()
            if (!doc.exists()) {
                Log.w("AuthRepo", "getWalletBalance: User document not found for UID: $uid")
                return Result.success(doc.getDouble("walletBalance") ?: 0.0)
            }
            Result.success(doc.getDouble("walletBalance") ?: 0.0)
        } catch (e: Exception) {
            Log.e("AuthRepo", "getWalletBalance failed", e)
            Result.failure(e)
        }
    }

    suspend fun updateWalletBalance(newBalance: Double): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Log.w("AuthRepo", "updateWalletBalance: No user logged in")
                return Result.failure(Exception("No user logged in to update wallet balance."))
            }
            usersCollection.document(uid)
                .update("walletBalance", newBalance)
                .await()
            Log.d("AuthRepo", "updateWalletBalance successful for UID: $uid")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepo", "updateWalletBalance failed", e)
            Result.failure(e)
        }
    }


}