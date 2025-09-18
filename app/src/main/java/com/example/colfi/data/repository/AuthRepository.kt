// AuthRepository.kt
package com.example.colfi.data.repository

import android.util.Log
import com.example.colfi.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.io.path.exists

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // Function to check if a user is logged in via Firebase Auth
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun getCurrentUser(): Result<User> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            try {
                val documentSnapshot = usersCollection.document(firebaseUser.uid).get().await()
                val currentUser = documentSnapshot.toObject(User::class.java)
                if (currentUser != null) {
                    Result.success(currentUser)
                } else {
                    Result.failure(Exception("User data not found in Firestore."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("No user logged in."))
        }
    }

    // Function for anonymous login
    suspend fun loginAsGuest(): Result<User> {
        return try {
            // 1. Sign in anonymously with Firebase Authentication.
            val authResult = auth.signInAnonymously().await()
            val firebaseUser = authResult.user ?: throw Exception("Anonymous sign-in failed unexpectedly.")

            // 2. Create a user object for this guest session.
            val guestUser = User(
                username = "guest_${firebaseUser.uid.take(6)}", // Creates a unique guest username like "guest_aB1cD2"
                displayName = "Guest",
                email = "", // Anonymous users do not have an email.
                role = "guest" // Set role as guest
            )

            // 3. Save this guest's data to Firestore. This is crucial for guests to have persistent data (like a shopping cart).
            usersCollection.document(firebaseUser.uid).set(guestUser).await()

            Log.d("AuthRepo", "Anonymous login successful for user: ${firebaseUser.uid}")
            Result.success(guestUser)

        } catch (e: Exception) {
            Log.e("AuthRepo", "Anonymous login failed: ${e.message}")
            Result.failure(e)
        }
    }

    // Login with Firebase
    suspend fun login(username: String, password: String): Result<User> {
        return try {
            // 1. Look up the email from Firestore by username
            val querySnapshot = usersCollection
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                throw Exception("Username not found")
            }

            val userDoc = querySnapshot.documents[0]
            val email = userDoc.getString("email") ?: throw Exception("Email not found")

            // 2. Sign in with email + password in FirebaseAuth
            // A try-catch here helps differentiate between wrong password and other issues
            val authResult = try {
                auth.signInWithEmailAndPassword(email, password).await()
            } catch (authError: Exception) {
                // This will catch wrong passwords, disabled users, etc.
                throw Exception("Invalid username or password")
            }

            // 3. Convert Firestore document into User object
            val user = userDoc.toObject(User::class.java)
                ?: throw Exception("Error converting user data")

            Log.d("AuthRepo", "Login successful: ${user.displayName}")
            Result.success(user)

        } catch (e: Exception) {
            Log.e("AuthRepo", "Login failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun registerUser(
        username: String,
        email: String,
        password: String,
        displayName: String,
        role: String,
        balance: Double = 0.0,
        points: Int = 0,
        tier: Int = 0
    ): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            // Save user data including role in Firestore
            val newUser = User(
                username = username,
                displayName = displayName,
                email = email,
                walletBalance = balance,
                points = points,
                vouchers = 0,
                role = role
            )

            usersCollection.document(firebaseUser.uid).set(newUser).await()

            Result.success("Registration successful")
        } catch (e: Exception) {
            Log.e("AuthRepo", "Registration failed: ${e.message}")
            Result.failure(e)
        }
    }

    // Logout
    fun logoutUser() {
        auth.signOut()
        Log.d("AuthRepo", "User logged out")
    }

    // Guest user (no login required)
    fun getGuestUser(): User {
        return User(
            username = "guest",
            displayName = "Guest",
            walletBalance = 0.0,
            role = "guest"
        )
    }
}